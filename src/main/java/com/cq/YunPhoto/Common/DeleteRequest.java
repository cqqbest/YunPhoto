package com.cq.YunPhoto.Common;

import lombok.Data;

import java.io.Serializable;

/**
 * 封装删除请求
 */

@Data
public class DeleteRequest implements Serializable {
    //删除的id
    private Long id;
    //序列化版本UID
    private static final Long SERIAL_VERSION_UID = 1L;

}
