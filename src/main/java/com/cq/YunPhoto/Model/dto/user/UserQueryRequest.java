package com.cq.YunPhoto.Model.dto.user;

import com.cq.YunPhoto.Common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
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
     * 用户角色
     */
    private String role;

    private static final long serialVersionUID = 1L;
}
