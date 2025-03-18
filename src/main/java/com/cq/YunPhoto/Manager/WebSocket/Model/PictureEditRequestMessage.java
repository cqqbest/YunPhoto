package com.cq.YunPhoto.Manager.WebSocket.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片编辑请求信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditRequestMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 执行的操作
     */
    private String editAction;

}
