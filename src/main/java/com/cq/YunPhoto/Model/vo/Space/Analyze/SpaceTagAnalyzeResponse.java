package com.cq.YunPhoto.Model.vo.Space.Analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间标签分析响应类
 */
@Data
public class SpaceTagAnalyzeResponse implements Serializable {

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签使用次数
     */
    private long tagCount;


    private static final long serialVersionUID = 1L;
}
