package com.cq.YunPhoto.Model.dto.Picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByColorRequest implements Serializable {
    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 空间id
     */
    private Long spaceId;
}
