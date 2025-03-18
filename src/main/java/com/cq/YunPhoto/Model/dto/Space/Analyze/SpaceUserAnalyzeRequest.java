package com.cq.YunPhoto.Model.dto.Space.Analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data


/**
 * 用户上传行为分析请求
 */
public class SpaceUserAnalyzeRequest extends spaceAnalyzeRequest {
    /**
     * 用户id
     */

    private Long userId;

    /**
     * 时间维度：Day，week，month
     */
    private String timeDimension;
}
