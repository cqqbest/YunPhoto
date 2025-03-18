package com.cq.YunPhoto.Model.dto.Picture;

import lombok.Data;

import java.io.Serializable;
@Data
public class PictureUpLoadByBatchRequest implements Serializable {
    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer Count = 20;

    /**
     * 名称前缀（默认为搜索词）
     */
    private String namePrefix;

    private static final long serialVersionUID = 1L;
}
