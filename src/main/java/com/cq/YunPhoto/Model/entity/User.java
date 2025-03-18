package com.cq.YunPhoto.Model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Date;
import lombok.Data;
import org.apache.ibatis.annotations.Delete;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)//雪花算法,使用连续的id容易被爬虫攻击
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

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
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic//逻辑删除，防止误删
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}