package com.cq.YunPhoto.Model.dto.Picture;

import lombok.Data;

import java.io.Serializable;


@Data
public class SearchPictureByPictureRequest implements Serializable {
    /**
     * 图片id
     */
    private Long pictureId;

    private final static long serialVersionUID = 1L;
}
