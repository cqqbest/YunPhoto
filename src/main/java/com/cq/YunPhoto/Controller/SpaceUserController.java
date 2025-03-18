package com.cq.YunPhoto.Controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.DeleteRequest;
import com.cq.YunPhoto.Common.ResultUtils;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Manager.auth.annotation.SaSpaceCheckPermission;
import com.cq.YunPhoto.Model.dto.SpaceUser.SpaceUserAddRequest;
import com.cq.YunPhoto.Model.dto.SpaceUser.SpaceUserEditRequest;
import com.cq.YunPhoto.Model.dto.SpaceUser.SpaceUserQueryRequest;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.SpaceUser;
import com.cq.YunPhoto.Model.enums.SpaceTypeEnum;
import com.cq.YunPhoto.Model.vo.SpaceUserVo;
import com.cq.YunPhoto.constant.spaceUserPermissionConstant;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.service.SpaceUserService;
import com.cq.YunPhoto.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 空间用户添加
     */
    @PostMapping("/addSpaceUser")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.SPACE_USER_MANGER)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest,HttpServletRequest request){
        //判空
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.NOT_FOUND_ERROR,"请求参数不存在");
        //调用接口
        Long id = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }

    /**
     * 空间用户移除
     */
    @PostMapping("/deleteSpaceUser")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.SPACE_USER_MANGER)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        //判空
        ThrowUtils.throwIf(deleteRequest == null,ErrorCode.NOT_FOUND_ERROR,"请求参数不存在");
        //判断是否存在
        ThrowUtils.throwIf(spaceUserService.getById(deleteRequest.getId()) == null,ErrorCode.NOT_FOUND_ERROR,"该空间用户不存在");
        //操作数据库
        boolean b = spaceUserService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!b,ErrorCode.OPERATION_ERROR,"移除失败");
        return ResultUtils.success(true);
    }

    /**
     * 查询空间某个成员信息
     */
    @PostMapping("/getSpaceUser")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.SPACE_USER_MANGER)
    public BaseResponse<SpaceUserVo> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request){
        //判空
        ThrowUtils.throwIf(spaceUserQueryRequest == null,ErrorCode.NOT_FOUND_ERROR,"请求参数不存在");
        //获取查询条件
        QueryWrapper<SpaceUser> spaceUserWrapper = spaceUserService.getSpaceUserWrapper(spaceUserQueryRequest);
        //查询
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserWrapper);
        //封装结果返回
        SpaceUserVo spaceUserVo = spaceUserService.getSpaceUserVo(spaceUser);
        return ResultUtils.success(spaceUserVo);

    }


    /**
     * 查询空间成员列表
     */
    @PostMapping("/getSpaceUserList")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.SPACE_USER_MANGER)
    public BaseResponse<List<SpaceUserVo>> getSpaceUserList(@RequestParam("spaceId") long spaceId, HttpServletRequest request){
        Space space = spaceService.getById(spaceId);
        //判断空间是否存在
        ThrowUtils.throwIf(space == null,ErrorCode.NOT_FOUND_ERROR,"该空间不存在");
        //判断空间是否为团队空间
        Integer spaceType = space.getSpaceType();
        ThrowUtils.throwIf(SpaceTypeEnum.getSpaceTypeEnumByCode(spaceType) != SpaceTypeEnum.TEAM,ErrorCode.NOT_FOUND_ERROR,"该空间不是团队空间");
        //构造查询条件
        QueryWrapper<SpaceUser> spaceUserWrapper = new QueryWrapper<>();
        spaceUserWrapper.eq("spaceId",spaceId);
        List<SpaceUser> list = spaceUserService.list(spaceUserWrapper);
        //封装结果返回
        List<SpaceUserVo> spaceUserList = spaceUserService.getSpaceUserList(list);
        return ResultUtils.success(spaceUserList);


    }


    /**
     * 编辑空间用户信息
     */
    @PostMapping("/editSpaceUser")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.SPACE_USER_MANGER)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest, HttpServletRequest request){
        //判空
        ThrowUtils.throwIf(spaceUserEditRequest == null,ErrorCode.NOT_FOUND_ERROR,"请求参数不存在");
        //数据填充
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserEditRequest,spaceUser);
        SpaceUser oldSpaceUser = spaceUserService.getById(spaceUser.getId());
        //判断空间用户是否存在
        ThrowUtils.throwIf(oldSpaceUser == null,ErrorCode.NOT_FOUND_ERROR,"该空间用户不存在");
        //填充其余数据
        spaceUser.setSpaceId(oldSpaceUser.getSpaceId());
        spaceUser.setUserId(oldSpaceUser.getUserId());
        //补充编辑时间
        spaceUser.setUpdateTime(new Date());
        //数据校验
        spaceUserService.checkSpaceUser(spaceUser);
        //操作数据库
        boolean b = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!b,ErrorCode.OPERATION_ERROR,"编辑失败");
        return ResultUtils.success(true);
    }
    /**
     *查看我加入的团队空间列表
     */
    @PostMapping("/getMySpaceList")
    public BaseResponse<List<SpaceUserVo>> getMySpaceList(HttpServletRequest request) {
        //获取当前登录用户
        userService.getLoginUser(request);
        //判断是否登录
        ThrowUtils.throwIf(userService.getLoginUser(request) == null, ErrorCode.NOT_FOUND_ERROR, "用户未登录");
        //获取用户id
        Long userId = userService.getLoginUser(request).getId();
        //构造查询条件
        QueryWrapper<SpaceUser> spaceUserWrapper = new QueryWrapper<>();
        spaceUserWrapper.eq("userId", userId);
        //查询
        List<SpaceUser> list = spaceUserService.list(spaceUserWrapper);
        //封装结果返回
        List<SpaceUserVo> spaceUserList = spaceUserService.getSpaceUserList(list);
        return ResultUtils.success(spaceUserList);

    }

}
