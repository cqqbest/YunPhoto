package com.cq.YunPhoto.Manager.WebSocket.disruptor;

import com.cq.YunPhoto.Manager.WebSocket.Model.PictureEditMessageType;
import com.cq.YunPhoto.Manager.WebSocket.Model.PictureEditRequestMessage;
import com.cq.YunPhoto.Manager.WebSocket.Model.PictureEditResponseMessage;
import com.cq.YunPhoto.Manager.WebSocket.PictureEditHandler;
import com.cq.YunPhoto.Model.vo.UserVO;
import com.lmax.disruptor.EventHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * 事件处理器(消费者)
 */
@Component
public class PictureEditEventWorkHandler implements EventHandler<PictureEditEvent> {

    @Resource
    @Lazy
    private PictureEditHandler pictureEditHandler;

    @Override
    public void onEvent(PictureEditEvent pictureEditEvent, long l, boolean b) throws Exception {
        PictureEditRequestMessage pictureEditRequestMessage = pictureEditEvent.getPictureEditRequestMessage();
        Long pictureId = pictureEditEvent.getPictureId();
        WebSocketSession session = pictureEditEvent.getSession();
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageType enumByValue = PictureEditMessageType.getEnumByValue(type);
        if (enumByValue != null) {
            switch (enumByValue) {
                case ENTER_EDIT :
                    pictureEditHandler.handleEnterEdit(session, pictureEditRequestMessage);
                    break;
                case EDIT_ACTION:
                    pictureEditHandler.handleEdit(session, pictureEditRequestMessage);
                    break;
                case EXIT_EDIT:
                    pictureEditHandler.handleExitEdit(session, pictureEditRequestMessage);
                    break;
                default:
                    //发送错误消息给其他客户端
                    PictureEditResponseMessage message1 = new PictureEditResponseMessage();
                    message1.setMessage("未知消息类型");
                    message1.setType(PictureEditMessageType.ERROR.getValue());
                    UserVO user = (UserVO)session.getAttributes().get("user");
                    message1.setUserVO(user);
                    pictureEditHandler.broadcastMessage(message1, pictureId);
            }
        }

    }
}
