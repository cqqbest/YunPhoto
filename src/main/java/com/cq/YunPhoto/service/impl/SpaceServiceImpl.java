package com.cq.YunPhoto.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Manager.auth.SpaceUserAuthManger;
import com.cq.YunPhoto.Manager.sharding.DynamicShardingManger;
import com.cq.YunPhoto.Model.dto.Space.SpaceAddRequest;
import com.cq.YunPhoto.Model.dto.Space.SpaceQueryRequest;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.SpaceUser;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.SpaceLevelEnum;
import com.cq.YunPhoto.Model.enums.SpaceTypeEnum;
import com.cq.YunPhoto.Model.enums.SpaceUserRoleEnum;
import com.cq.YunPhoto.Model.vo.Space.SpaceVo;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.mapper.SpaceMapper;
import com.cq.YunPhoto.service.SpaceUserService;
import com.cq.YunPhoto.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 86198
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-03-04 13:57:00
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    private UserService userService;

    @Resource//事务管理,在锁中用
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserServices;

    @Resource
    private SpaceUserAuthManger spaceUserAuthManger;

    @Resource
    @Lazy
    private DynamicShardingManger dynamicShardingManger;

    /**
     * 空间数据校验
     * @param space
     */
    @Override
    public void checkSpace(Space space) {
        ThrowUtils.throwIf(space.getSpaceName() == null, ErrorCode.PARAMS_ERROR,"空间名不能为空");
        ThrowUtils.throwIf(space.getSpaceName().length()>30, ErrorCode.PARAMS_ERROR,"空间名长度不能超过30");
        ThrowUtils.throwIf(ObjUtil.isEmpty(SpaceLevelEnum.getSpaceLevelEnum(space.getSpaceLevel())), ErrorCode.PARAMS_ERROR,"空间等级错误");
        ThrowUtils.throwIf(space.getSpaceLevel() == null, ErrorCode.PARAMS_ERROR,"空间等级不能为空");
        ThrowUtils.throwIf(SpaceTypeEnum.getSpaceTypeEnumByCode(space.getSpaceType()) == null, ErrorCode.PARAMS_ERROR,"空间类型错误");
        ThrowUtils.throwIf(space.getSpaceType() == null, ErrorCode.PARAMS_ERROR,"空间类型不能为空");

    }

    /**
     * 空间数填充
     * @param space
     */
    @Override
    public void fillSpace(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getSpaceLevelEnum(space.getSpaceLevel());
        ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAMS_ERROR,"空间等级错误");
        if(space.getMaxCount() == null) {
            space.setMaxCount(spaceLevelEnum.getMaxCount());
        }
        if(space.getMaxSize() == null) {
            space.setMaxSize(spaceLevelEnum.getMaxSize());
        }
    }

    /**
     * 用户空间创建
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public Long createSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest,space);
        //填充默认值
        if(spaceAddRequest.getSpaceLevel() == null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if(spaceAddRequest.getSpaceName() == null){
            space.setSpaceName("默认空间");
        }
        if(spaceAddRequest.getSpaceType() == null){
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getCode());
        }
        //默认空间大小和存储量填充
        fillSpace(space);
        //校验
        checkSpace(space);
        space.setUserId(loginUser.getId());
        space.setCreateTime(new Date());
        //实现一个用户只能创建一个用户空间（加锁和事物）
        String lock = String.valueOf(space.getUserId()).intern();
        //加锁
        synchronized (lock) {
            Long execute = transactionTemplate.execute(status -> {

                //判断用户空间是否存在（普通用户只能创建一个私有空间和团队空间）
                Boolean admin = userService.isAdmin(loginUser);
                if(!admin) {
                    Space userSpace = this.getOne(new QueryWrapper<Space>()
                            .eq("userId", loginUser.getId())
                            .eq("spaceType", spaceAddRequest.getSpaceType()));
                    if (userSpace != null) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "每个用户只能创建一个私有空间和团队空间");
                    }
                }
                //如果创键的是团队空间，则创建者为管理员
                if(space.getSpaceType().equals(SpaceTypeEnum.TEAM.getCode())){
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(loginUser.getId());
                    spaceUser.setSpaceRole(SpaceUserRoleEnum.ADMIN.getValue());
                    spaceUser.setCreateTime(new Date());
                    boolean save = spaceUserServices.save(spaceUser);
                    ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建失败");
                }
                //在创建团队空间时创建团队图片分表(上线时应该为旗舰版创建图片分表)
                if(space.getSpaceType().equals(SpaceTypeEnum.TEAM.getCode())){
                    dynamicShardingManger.createTable(space);
                }
                this.save(space);
                return space.getId();
            });
            return Optional.ofNullable(execute).orElse(-1L);
        }
    }

    /**
     *
     * @param spaceQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Space> queryPageWrapper(SpaceQueryRequest spaceQueryRequest) {
        Long id = spaceQueryRequest.getId();
        String spaceName = spaceQueryRequest.getSpaceName();
        int spaceLevel = spaceQueryRequest.getSpaceLevel();
        String order = spaceQueryRequest.getOrder();
        Long userId = spaceQueryRequest.getUserId();
        int spaceType = spaceQueryRequest.getSpaceType();
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(ObjUtil.isEmpty(order),order.equals("descend"),order);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        return queryWrapper;
    }


    /**
     * 获取空间视图分页
     * @param page
     * @param request
     * @return
     */
    @Override
    public Page<SpaceVo> querySpaceVoPage(Page<Space> page, HttpServletRequest request) {
        List<Space> spaceList = page.getRecords();
        Page<SpaceVo> spaceVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVo> spaceVOList = spaceList.stream().map(SpaceVo::toSpaceVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
            // 3. 获取权限列表(自己添加)
            Space space = this.getById(spaceVO.getId());
            List<String> spaceUserPermissionList = spaceUserAuthManger.getSpaceUserPermissionList(space, user);
            spaceVO.setPermissionList(spaceUserPermissionList);
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }
}




