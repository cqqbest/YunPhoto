package com.cq.YunPhoto.Model.dto.Space.Analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用排名分析，默认输出前十名（管理员使用）
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前N的空间
     */
    private Integer topN;

    private static final long serialVersionUID = 1L;
}
