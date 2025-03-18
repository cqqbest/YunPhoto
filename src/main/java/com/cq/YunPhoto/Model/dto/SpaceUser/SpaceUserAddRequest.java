package com.cq.YunPhoto.Model.dto.SpaceUser;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间用户添加请求
 */
@Data
public class SpaceUserAddRequest implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;


    private static final long serialVersionUID = 1L;
}
