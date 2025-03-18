package com.cq.YunPhoto.Model.dto.Space.Analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class spaceAnalyzeRequest implements Serializable {

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private boolean isPublic;

    /**
     * 是否查询全部图库
     */
    private boolean isAll;

    private static final long serialVersionUID = 1L;
}
