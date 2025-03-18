package com.cq.YunPhoto.Model.vo.Space.Analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间图片分类分析响应
 */
@Data
public class SpaceCateGoryAnalyzeResponse implements Serializable {

    /**
     * 图片分类
     */
    private String category;
    /**
     * 分类图片数量
     */
    private Long count;
    /**
     * 分类图片大小
     */
    private Long size;



    private static final long serialVersionUID = 1L;

}
