package com.cq.YunPhoto.Manager.WebSocket.disruptor;

import com.cq.YunPhoto.Manager.WebSocket.Model.PictureEditRequestMessage;
import com.cq.YunPhoto.Manager.WebSocket.Model.PictureEditResponseMessage;
import com.cq.YunPhoto.Model.vo.UserVO;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;


/**
 * disrupt事件模型
 */
@Data
public class PictureEditEvent {

    /**
     * 图片编辑请求消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前会话
     */
    private WebSocketSession session;

    /**
     *当前图片id
     */
    private Long pictureId;

    /**
     * 当前用户
     */
    private UserVO user;
}
