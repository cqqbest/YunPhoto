package com.cq.YunPhoto.Model.dto.user;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserUpdateRequest  implements Serializable {


    private static final long serialVersionUID = 1L;


    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

}
