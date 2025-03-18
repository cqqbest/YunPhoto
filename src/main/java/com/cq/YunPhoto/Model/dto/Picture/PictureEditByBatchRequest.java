package com.cq.YunPhoto.Model.dto.Picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditByBatchRequest implements Serializable {

    /**
     * 图片id列表
     */
    private List<Long> pictureIds;
    /**
     * 空间id
     */
    private String spaceId;
    /**
     * 标签
     */
    private List<String> tags;
    /**
     * 分类
     */
    private String category;
    /**
     * 命名规则
     */
    private String namingRule;


    private static final long serialVersionUID = 1L;
}
