package com.cq.YunPhoto.Model.dto.Space;

import lombok.Data;

import java.io.Serializable;

/**
 *空间编辑请求
 */
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     *  空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
