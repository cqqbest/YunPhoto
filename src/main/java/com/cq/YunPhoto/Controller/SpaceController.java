package com.cq.YunPhoto.Controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cq.YunPhoto.Annotation.AuthCheck;
import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.DeleteRequest;
import com.cq.YunPhoto.Common.ResultUtils;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Manager.auth.SpaceUserAuthManger;
import com.cq.YunPhoto.Model.dto.Space.SpaceAddRequest;
import com.cq.YunPhoto.Model.dto.Space.SpaceEditRequest;
import com.cq.YunPhoto.Model.dto.Space.SpaceQueryRequest;
import com.cq.YunPhoto.Model.dto.Space.SpaceUpdateRequest;
import com.cq.YunPhoto.Model.entity.Picture;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.SpaceLevelEnum;
import com.cq.YunPhoto.Model.vo.Space.SpaceLevel;
import com.cq.YunPhoto.Model.vo.Space.SpaceVo;
import com.cq.YunPhoto.Model.vo.UserLoginVo;
import com.cq.YunPhoto.Model.vo.UserVO;
import com.cq.YunPhoto.constant.UserConstant;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private SpaceUserAuthManger spaceUserAuthManger;

    /**
     * 用户空间创建
     *
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/createSpace")
    public BaseResponse<Long> createSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        final User loginUser = userService.getLoginUser(request);
        //调用service层方法
        long spaceId  = spaceService.createSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId);
    }

    /**
     * 用户空间删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/deleteSpace")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //判断用户权限，只有空间创始人和管理员可以删除
        Long spaceId = deleteRequest.getId();
        boolean equals = loginUser.getId().equals(spaceId);
        Boolean admin = userService.isAdmin(loginUser);
        ThrowUtils.throwIf(!equals && !admin, ErrorCode.NOT_FOUND_ERROR, "权限不足");
        //操作数据库
        boolean b = spaceService.removeById(spaceId);
        ThrowUtils.throwIf(!b, ErrorCode.NOT_FOUND_ERROR, "删除失败");
        //删除空间内关联的图片(自己扩展)
        LambdaQueryChainWrapper<Picture> select = pictureService.lambdaQuery().eq(Picture::getSpaceId, spaceId).select();
        List<Picture> list = select.list();
        if(ObjUtil.isNotEmpty(list)) {
            boolean b1 = pictureService.removeBatchByIds(list);
            ThrowUtils.throwIf(!b1, ErrorCode.NOT_FOUND_ERROR, "删除失败");
            for (Picture picture : list) {
                pictureService.deletePictureFromCos(picture);
            }
        }else {
            return ResultUtils.success(true);
        }
        return ResultUtils.success(true);

    }


    /**
     * 用户空间更新(管理员用)
     *
     * @param spaceUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/updateSpace")
    @AuthCheck(value = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(spaceUpdateRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        //设置更新时间
        space.setUpdateTime(new Date());
        //如果maxCount和MaxSize为空，填充默认值
        spaceService.fillSpace(space);
        //数据校验
        spaceService.checkSpace(space);
        //操作数据库
        boolean b = spaceService.updateById(space);
        ThrowUtils.throwIf(!b, ErrorCode.NOT_FOUND_ERROR, "更新失败");
        return ResultUtils.success(true);

    }


    /**
     * 用户空间编辑(用户用)
     *
     * @param spaceEditRequest
     * @param request
     * @return
     */
    @PostMapping("/editSpace")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(spaceEditRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        final User loginUser = userService.getLoginUser(request);
        //判断用户权限，只有空间创始人和管理员可以编辑
        boolean equals = loginUser.getId().equals(spaceEditRequest.getId());
        Boolean admin = userService.isAdmin(loginUser);
        ThrowUtils.throwIf(!admin&&!equals, ErrorCode.NOT_FOUND_ERROR, "权限不足");
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        //设置编辑时间
        space.setEditTime(new Date());
        //操作数据库
        boolean b = spaceService.updateById(space);
        ThrowUtils.throwIf(!b, ErrorCode.NOT_FOUND_ERROR, "编辑失败");
        return ResultUtils.success(true);

    }

    /**
     * 根据id查询用户空间（管理员不需要脱敏）
     */
    @PostMapping("/getSpaceById")
    @AuthCheck(value = UserConstant.USER_LOGIN_STATE)
    public BaseResponse<Space> getSpaceById(@RequestParam Long id) {
        //判断id是否为空
        ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        Space byId = spaceService.getById(id);
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        return ResultUtils.success(byId);
    }

    /**
     * 根据id查询用户空间（用户需要脱敏）
     */
    @PostMapping("/getSpaceByIdUser")
    public BaseResponse<SpaceVo> getSpaceVoById(@RequestParam Long id, HttpServletRequest request) {
        //判断id是否为空
        ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        //获取脱敏后的空间信息
        SpaceVo spaceVo = SpaceVo.toSpaceVo(space);
        User loginUser = userService.getLoginUser(request);
        UserVO userVO = userService.getUserVO(loginUser);
        spaceVo.setUser(userVO);
        //获取当前用户的权限列表
        List<String> permissionList = spaceUserAuthManger.getSpaceUserPermissionList(space, loginUser);
        spaceVo.setPermissionList(permissionList);
        return ResultUtils.success(spaceVo);

    }

    /**
     * 获取用户空间分页（管理员，不需要脱敏）
     */
    @PostMapping("/getSpacePage")
    @AuthCheck(value = UserConstant.USER_LOGIN_STATE)
    public BaseResponse<Page<Space>> getSpacePage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取页数和大小
        long pageNum = spaceQueryRequest.getPageNum();
        long pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(pageNum, pageSize),spaceService.queryPageWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     *获取用户空间视图分页（用户，需要脱敏）
     */
    @PostMapping("/getSpacePage/vo")
    public BaseResponse<Page<SpaceVo>> getSpaceVo(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request){
        //判断请求是否为空
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取页数和大小
        long pageNum = spaceQueryRequest.getPageNum();
        long pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(pageNum, pageSize),spaceService.queryPageWrapper(spaceQueryRequest));
        //获取脱敏后的图片分页
        Page<SpaceVo> spaceVoPage = spaceService.querySpaceVoPage(spacePage,request);
        //返回分页
        return ResultUtils.success(spaceVoPage);
    }
    /**
     * 向前端展示用户空间等级
     */
    @GetMapping("/getSpaceLevel")
    public BaseResponse<List<SpaceLevel>> getSpaceLevel() {
        List<SpaceLevel> spaceLevels = new ArrayList<>();
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            spaceLevels.add(new SpaceLevel(spaceLevelEnum.getText(), spaceLevelEnum.getValue(), spaceLevelEnum.getMaxCount(), spaceLevelEnum.getMaxSize()));
        }
        return ResultUtils.success(spaceLevels);
    }



}
