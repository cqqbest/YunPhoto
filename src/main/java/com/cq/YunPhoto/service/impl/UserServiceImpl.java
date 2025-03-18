package com.cq.YunPhoto.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Manager.auth.StpKit;
import com.cq.YunPhoto.Model.dto.user.UserQueryRequest;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.UserRoleEnum;
import com.cq.YunPhoto.Model.vo.UserLoginVo;
import com.cq.YunPhoto.Model.vo.UserVO;
import com.cq.YunPhoto.service.UserService;
import com.cq.YunPhoto.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.RequestContextFilter;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.cq.YunPhoto.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author 86198
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-02-25 15:23:05
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    private final RequestContextFilter requestContextFilter;

    public UserServiceImpl(RequestContextFilter requestContextFilter) {
        this.requestContextFilter = requestContextFilter;
    }

    @Override
    public Boolean userRegister(String userAccount, String userPassword, String checkPassword) {
        //数据校验
        ThrowUtils.throwIf(userAccount.length()<4||userAccount.length()>12, ErrorCode.PARAMS_ERROR,"账号长度小于4或大于12");
        ThrowUtils.throwIf(userPassword.length()<8||userPassword.length()>16, ErrorCode.PARAMS_ERROR,"密码长度小于8或大于16");
        ThrowUtils.throwIf(ObjUtil.isEmpty(userAccount), ErrorCode.PARAMS_ERROR,"账号不能为空");
        ThrowUtils.throwIf(ObjUtil.isEmpty(userPassword), ErrorCode.PARAMS_ERROR,"密码不能为空");
        ThrowUtils.throwIf(ObjUtil.isEmpty(checkPassword), ErrorCode.PARAMS_ERROR,"确认密码不能为空");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR,"两次密码不一致");
        //判断账号是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long l = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(l>0, ErrorCode.PARAMS_ERROR,"账号已存在");
        //加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        //创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getCode());
        user.setUserName("用户"+userAccount);
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR,"注册失败");
        return result;
    }

    @Override
    public UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        ThrowUtils.throwIf(userAccount.length()<4||userAccount.length()>12, ErrorCode.PARAMS_ERROR,"账号长度小于4或大于12");
        ThrowUtils.throwIf(userPassword.length()<8||userPassword.length()>16, ErrorCode.PARAMS_ERROR,"密码长度小于8或大于16");
        ThrowUtils.throwIf(ObjUtil.isEmpty(userAccount), ErrorCode.PARAMS_ERROR,"账号不能为空");
        ThrowUtils.throwIf(ObjUtil.isEmpty(userPassword), ErrorCode.PARAMS_ERROR,"密码不能为空");
        //判断账号是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(user==null, ErrorCode.PARAMS_ERROR,"账号不存在");
        //判断密码是否正确
        String encryptPassword = getEncryptPassword(userPassword);
        ThrowUtils.throwIf(!encryptPassword.equals(user.getUserPassword()), ErrorCode.PARAMS_ERROR,"密码错误");
        //记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        //记录登录状态到Sa-Token空间体系
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE,user);
        return this.getUserLoginVo(user);


    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        //判断当前用户是否登录
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(user==null, ErrorCode.OPERATION_ERROR,"用户未登录");
        //从数据库获得用户信息
        User userInfo = this.getById(user.getId());
        return userInfo;
    }

    @Override
    public Boolean userLogout(HttpServletRequest request) {
        //判断当前用户是否登录
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(user==null, ErrorCode.OPERATION_ERROR,"用户未登录");
        //清除用户登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /*
    @Override
    public UserGetVo getUserGetVo(User user) {
        UserGetVo userGetVo = new UserGetVo();
        BeanUtil.copyProperties(user,userGetVo);
        return userGetVo;
    }

     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "cq";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public UserVO getUserVO(User user) {
        ThrowUtils.throwIf(user==null, ErrorCode.PARAMS_ERROR,"用户不存在");
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVoList(List<User> userList) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(userList), ErrorCode.PARAMS_ERROR,"用户列表为空");
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    @Override
    public QueryWrapper<User> queryPageWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest==null, ErrorCode.PARAMS_ERROR,"查询条件为空");
        Long id = userQueryRequest.getId();
        String order = userQueryRequest.getOrder();
        String role = userQueryRequest.getRole();
        String userAccount = userQueryRequest.getUserAccount();
        String userAvatar = userQueryRequest.getUserAvatar();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id),"id",id);
        queryWrapper.orderBy(ObjUtil.isEmpty(order),order.equals("descend"),order);
        queryWrapper.eq(ObjUtil.isNotNull(role),"role",role);
        queryWrapper.like(ObjUtil.isNotNull(userAccount),"userAccount",userAccount);
        queryWrapper.like(ObjUtil.isNotNull(userAvatar),"userAvatar",userAvatar);
        queryWrapper.like(ObjUtil.isNotNull(userName),"userName",userName);
        queryWrapper.like(ObjUtil.isNotNull(userProfile),"userProfile",userProfile);
        return queryWrapper;

    }

    /**
     * 判断用户是否为管理员
     * @param user
     * @return
     */
    @Override
    public Boolean isAdmin(User user) {
        //判空
        ThrowUtils.throwIf(user==null, ErrorCode.PARAMS_ERROR,"用户不存在");
        return user.getUserRole().equals(UserRoleEnum.ADMIN.getCode());
    }

    @Override
    public UserLoginVo getUserLoginVo(User user){
        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtil.copyProperties(user,userLoginVo);
        return userLoginVo;
    }

}




