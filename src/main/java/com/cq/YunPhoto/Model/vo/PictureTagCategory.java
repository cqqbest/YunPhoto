package com.cq.YunPhoto.Model.vo;

import lombok.Data;

import java.util.List;

/**
 * 图片标签分类列表
 */
@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagsList;

    /**
     * 分类列表
     */

    private List<String> categoryList;
}
