package com.cq.YunPhoto.Manager.WebSocket.Model;

import com.cq.YunPhoto.Model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.asm.Advice;

/**
 * 图片编辑响应消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditResponseMessage {

    /**
     * 执行的操作
     */
    private String editAction;

    /**
     * 信息
     */
    private String message;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 用户信息
     */
    private UserVO userVO;
}
