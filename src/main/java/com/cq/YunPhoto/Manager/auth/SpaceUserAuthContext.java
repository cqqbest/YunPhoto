package com.cq.YunPhoto.Manager.auth;

import com.cq.YunPhoto.Model.entity.Picture;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.SpaceUser;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
/**
 * 接受请求中的数据
 */
public class SpaceUserAuthContext {

    /**
     * 临时id，用于接受同请求的id参数
     */
    private Long id;

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     *空间用户id
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 空间用户信息
     */
    private SpaceUser spaceUser;


}
