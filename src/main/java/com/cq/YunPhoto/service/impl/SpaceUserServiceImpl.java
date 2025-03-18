package com.cq.YunPhoto.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.dto.SpaceUser.SpaceUserAddRequest;
import com.cq.YunPhoto.Model.dto.SpaceUser.SpaceUserQueryRequest;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.SpaceUser;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.SpaceUserRoleEnum;
import com.cq.YunPhoto.Model.vo.Space.SpaceVo;
import com.cq.YunPhoto.Model.vo.SpaceUserVo;
import com.cq.YunPhoto.Model.vo.UserVO;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.service.SpaceUserService;
import com.cq.YunPhoto.mapper.SpaceUserMapper;
import com.cq.YunPhoto.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author 86198
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-03-09 15:20:05
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    @Resource
    private UserService userService;


    @Resource
    @Lazy
    private SpaceService spaceService;


    /**
     * 数据校验
     */
    @Override
    public void checkSpaceUser(SpaceUser spaceUser) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        ThrowUtils.throwIf(userService.getById(spaceUser.getUserId()) == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(spaceService.getById(spaceUser.getSpaceId()) == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(SpaceUserRoleEnum.getEnumByValue(spaceUser.getSpaceRole()) == null,ErrorCode.NOT_FOUND_ERROR,"用户角色不存在");
    }

    /**
     * 空间用户添加
     * @param spaceUserAddRequest
     * @return
     */
    @Override
    public Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        //判空
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //数据填充
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserAddRequest, spaceUser);
        //数据校验
        this.checkSpaceUser(spaceUser);
        //保存
        this.save(spaceUser);
        //返回id
        return spaceUser.getId();
    }

    /**
     * 获取空间用户视图
     * @param spaceUser
     * @return
     */

    @Override
    public SpaceUserVo getSpaceUserVo(SpaceUser spaceUser) {
        //数据校验
        checkSpaceUser(spaceUser);
        //构造视图
        SpaceUserVo spaceUserVo= new SpaceUserVo();
        BeanUtil.copyProperties(spaceUser,spaceUserVo);
        //获取相关空间视图
        Space space = spaceService.getById(spaceUser.getSpaceId());
        if(space != null) {
            SpaceVo spaceVo = SpaceVo.toSpaceVo(space);
            spaceUserVo.setSpaceVo(spaceVo);
        }
        //获取相关用户视图
        User user = userService.getById(spaceUser.getUserId());
        if(user != null) {
            UserVO userVO = userService.getUserVO(user);
            spaceUserVo.setUserVo(userVO);
        }
        //返回
        return spaceUserVo;

    }

    /**
     * 获取空间用户列表视图
     * @param spaceUserList
     * @return
     */


    @Override
    public List<SpaceUserVo> getSpaceUserList(List<SpaceUser> spaceUserList) {
        //数据校验
        spaceUserList.forEach(this::checkSpaceUser);
        //构造视图
        List<SpaceUserVo> spaceUserVoList = new ArrayList<>();
        spaceUserList.forEach(spaceUser -> {
            spaceUserVoList.add(getSpaceUserVo(spaceUser));
        });
        //返回
        return spaceUserVoList;

    }



    /**
     * 构造空间用户查询条件
     * @param spaceUserQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<SpaceUser> getSpaceUserWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        //判空
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取请求数据
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        Long id = spaceUserQueryRequest.getId();
        //构造查询条件
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(spaceRole), "space_role", spaceRole);
        queryWrapper.eq(spaceId != null, "space_id", spaceId);
        queryWrapper.eq(userId != null, "user_id", userId);
        queryWrapper.eq(id != null, "id", id);
        //返回结果
        return queryWrapper;
    }


}




