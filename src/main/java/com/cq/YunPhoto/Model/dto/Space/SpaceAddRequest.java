package com.cq.YunPhoto.Model.dto.Space;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间创建请求
 */

@Data
public class SpaceAddRequest implements Serializable {
    /**
     *  空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间类型：私有/团队
     */
    private Integer spaceType;

    private static final long serialVersionUID = 1L;
}
