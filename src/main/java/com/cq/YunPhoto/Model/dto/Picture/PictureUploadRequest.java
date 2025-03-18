package com.cq.YunPhoto.Model.dto.Picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传请求
 */

@Data
public class PictureUploadRequest implements Serializable {

    private Long id;

    private String url;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 空间id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
