package com.cq.YunPhoto.Manager.auth.model;

import lombok.Data;

import java.io.Serializable;


/**
 * 用户空间权限模型
 */
@Data
public class spaceUserPermission implements Serializable {

    private String key;

    private String name;

    private String description;

    private static final long serialVersionUID = 1L;
}
