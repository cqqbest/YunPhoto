package com.cq.YunPhoto.Manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *获取spaceUserAuthConfig的值
 */

@Data
public class spaceUserAuthConfig implements Serializable {

    /**
     * 权限列表
     */
    private List<spaceUserPermission> permission;

    /**
     * 角色列表
     */
    private List<spaceUserRole> role;

    private static final long serialVersionUID = 1L;

}
