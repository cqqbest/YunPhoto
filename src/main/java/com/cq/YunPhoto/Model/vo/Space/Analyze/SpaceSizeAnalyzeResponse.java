package com.cq.YunPhoto.Model.vo.Space.Analyze;

import lombok.Data;

import java.io.Serializable;

/**
 *空间图片大小分析响应
 */
@Data
public class SpaceSizeAnalyzeResponse implements Serializable {

    /**
     * 图片大小范围
     */
    private String sizeRange;

    /**
     * 图片数量
     */
    private Long count;


    private static final long serialVersionUID = 1L;
}
