package com.cq.YunPhoto.Model.dto.Space;

import com.cq.YunPhoto.Common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间查询请求
 */

@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 空间类型：团队/私有
     */
    private Integer spaceType;


    private static final long serialVersionUID = 1L;
}
