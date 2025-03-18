package com.cq.YunPhoto.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cq.YunPhoto.Model.dto.user.UserQueryRequest;
import com.cq.YunPhoto.Model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cq.YunPhoto.Model.vo.UserLoginVo;
import com.cq.YunPhoto.Model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86198
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-02-25 15:23:05
*/
public interface UserService extends IService<User> {

    Boolean userRegister(String userAccount, String userPassword, String checkPassword);

    UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    Boolean userLogout(HttpServletRequest request);

    UserLoginVo getUserLoginVo(User user);

    //UserGetVo getUserGetVo(User user);

    String getEncryptPassword(String userPassword);

    UserVO getUserVO(User user);

    List<UserVO> getUserVoList(List<User> userList);

    QueryWrapper<User> queryPageWrapper(UserQueryRequest userQueryRequest);

    Boolean isAdmin(User user);
}
