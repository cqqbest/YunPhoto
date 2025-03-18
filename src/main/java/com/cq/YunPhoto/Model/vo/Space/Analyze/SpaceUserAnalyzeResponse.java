package com.cq.YunPhoto.Model.vo.Space.Analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户上传分析响应
 */

@Data
public class SpaceUserAnalyzeResponse implements Serializable {

    /**
     * 时间区间
     */
    private String time;

    /**
     * 上传数量
     */
    private long uploadNum;


    private static final long serialVersionUID = 1L;
}

