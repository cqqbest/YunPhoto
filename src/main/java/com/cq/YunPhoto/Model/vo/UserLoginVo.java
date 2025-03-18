package com.cq.YunPhoto.Model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 对返回给前端的用户数据进行脱敏
 */
@Data
public class UserLoginVo implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 3191241716373120793L;
}
