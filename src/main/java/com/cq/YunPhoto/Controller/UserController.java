package com.cq.YunPhoto.Controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cq.YunPhoto.Annotation.AuthCheck;
import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.DeleteRequest;
import com.cq.YunPhoto.Common.ResultUtils;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.dto.user.*;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.vo.UserLoginVo;
import com.cq.YunPhoto.Model.vo.UserVO;
import com.cq.YunPhoto.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.cq.YunPhoto.constant.UserConstant.ADMIN_ROLE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    //用户注册
    @PostMapping("/register")
    public BaseResponse<?> register(@RequestBody UserRegisterRequest userRegisterRequest){
        //判断请求是否为空
        ThrowUtils.throwIf(userRegisterRequest == null,ErrorCode.PARAMS_ERROR);
        //调用service层方法
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        Boolean result = userService.userRegister(userAccount,userPassword,checkPassword);
        return ResultUtils.success(result);
    }
    //用户登录
    @PostMapping("/login")
    public BaseResponse<UserLoginVo> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        //判断请求是否为空
        ThrowUtils.throwIf(userLoginRequest == null,ErrorCode.PARAMS_ERROR);
        //调用service层方法
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        UserLoginVo userLoginVo = userService.userLogin(userAccount,userPassword,request);
        return ResultUtils.success(userLoginVo);
    }
    //获取当前登录用户信息
    @GetMapping("/get/login")
    //@AuthCheck(value = ADMIN_ROLE)
    public BaseResponse<UserLoginVo> getUserLogin(HttpServletRequest request){
        //判断请求是否为空
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        //调用service层方法
        User user = userService.getLoginUser(request);
        UserLoginVo userLoginVo = userService.getUserLoginVo(user);
        return ResultUtils.success(userLoginVo);
    }
    //用户注销
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request){
        //判断请求是否为空
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        //调用service层方法
        Boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
    //用户管理crud
    //添加用户
    @PostMapping("/add")
    @AuthCheck(value = ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        //判断请求是否为空
        ThrowUtils.throwIf(userAddRequest == null,ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest,user);
        //设置默认密码
        user.setUserPassword(userService.getEncryptPassword("123456789"));
        //将用户保存到数据库
        final boolean result = userService.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }
    //根据id删除用户
    @PostMapping("/delete")
    @AuthCheck(value = ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
        //判断请求是否为空
        ThrowUtils.throwIf(deleteRequest == null,ErrorCode.PARAMS_ERROR);
        //删除数据中的数据
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }
    //根据id获取用户（管理员，未脱敏）
    @GetMapping("/get")
    @AuthCheck(value = ADMIN_ROLE)
    public BaseResponse<User> getUserById(@RequestParam Long id){
        //判断请求是否为空
        ThrowUtils.throwIf(id == null,ErrorCode.PARAMS_ERROR);
        //获取用户数据
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }
    //根据id获取用户视图（脱敏）
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserByIdVo(@RequestParam Long id){
        //判断请求是否为空
        ThrowUtils.throwIf(id == null,ErrorCode.PARAMS_ERROR);
        //获取用户数据
        BaseResponse<User> userResponse = getUserById(id);
        User user = userResponse.getData();
        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }
    //修改用户
    @PostMapping("/update")
    @AuthCheck(value = ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        //判断请求是否为空
        ThrowUtils.throwIf(userUpdateRequest == null,ErrorCode.PARAMS_ERROR);
        //判断用户是否存在
        BaseResponse<User> userResponse = getUserById(userUpdateRequest.getId());
        User user = userResponse.getData();
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR);
        //更改用户信息
        BeanUtil.copyProperties(userUpdateRequest,user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    //分页获取用户封装列表
    @GetMapping("/list/page/vo")
    @AuthCheck(value = ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> getUserList(UserQueryRequest userQueryRequest){
        //判断请求是否为空
        ThrowUtils.throwIf(userQueryRequest == null,ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        //获取用户列表
        QueryWrapper<User> userQueryWrapper = userService.queryPageWrapper(userQueryRequest);
        Page<User> userPage = userService.page(new Page<>(pageNum, pageSize), userQueryWrapper);
        //封装用户列表
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVoList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);

    }

}
