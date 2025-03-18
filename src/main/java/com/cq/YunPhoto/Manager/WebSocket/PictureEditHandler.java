package com.cq.YunPhoto.Manager.WebSocket;

import com.cq.YunPhoto.Manager.WebSocket.Model.PictureEditMessageType;
import com.cq.YunPhoto.Manager.WebSocket.Model.PictureEditRequestMessage;
import com.cq.YunPhoto.Manager.WebSocket.Model.PictureEditResponseMessage;
import com.cq.YunPhoto.Manager.WebSocket.disruptor.PictureEditEvent;
import com.cq.YunPhoto.Manager.WebSocket.disruptor.PictureEditEventProducer;
import com.cq.YunPhoto.Model.vo.UserVO;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.service.SpaceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {


    @Resource
    private PictureService pictureService;
    @Resource
    private SpaceService spaceService;

    @Resource
    PictureEditEventProducer pictureEditEventProducer;


    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private static final Map<Long, Long> pictureEditUser = new ConcurrentHashMap();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private  final Map<Long, Set<WebSocketSession>> pictureSession = new ConcurrentHashMap();
    private final ObjectMapper objectMapper;

    public PictureEditHandler(@Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    //广播消息的方法(排除给编辑者本人)
    public void broadcastMessage(WebSocketSession excludeSession, PictureEditResponseMessage message, Long pictureId) throws IOException {
        Set<WebSocketSession> sessions = pictureSession.get(pictureId);
        if (sessions != null) {
            //将bean序列化为Json格式
            ObjectMapper objectMapper = new ObjectMapper();
            //设置序列化规则,防止Long类型被序列化为科学计数法，精度丢失
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(simpleModule);
            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session != null && session.equals(excludeSession)) {
                    continue;
                }
                if (session != null && session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    //广播消息的方法（不排除给编辑者本人)
    public void broadcastMessage(PictureEditResponseMessage message, Long pictureId) throws IOException {
        broadcastMessage(null, message, pictureId);
    }


    //实现连接建立成功后执行的方法，保存会话到集合中，并且给其他会话发送消息
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //将会话保存到集合中
        Long pictureId = (Long)session.getAttributes().get("pictureId");
        if (pictureSession.containsKey(pictureId)) {
            pictureSession.get(pictureId).add(session);
        }
        else {
            Set<WebSocketSession> sessions = new ConcurrentHashSet<>();
            sessions.add(session);
            pictureSession.put(pictureId, sessions);
        }
        //给其他会话发送消息
        PictureEditResponseMessage message = new PictureEditResponseMessage();
        UserVO user = (UserVO) session.getAttributes().get("user");
        message.setMessage(user.getUserName() + "进入了编辑状态");
        message.setType(PictureEditMessageType.INFO.getValue());
        message.setUserVO(user);
        broadcastMessage(message, pictureId);
    }

    //编写接收客户端消息的方法，根据消息类别执行不同的处理
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //将消息反序列化为bean
        PictureEditRequestMessage requestMessage = objectMapper.
                readValue(message.getPayload(), PictureEditRequestMessage.class);
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        UserVO user = (UserVO) attributes.get("user");
        Set<WebSocketSession> webSocketSessions = pictureSession.get(pictureId);
        if (webSocketSessions == null) {
            log.error("当前图片没有用户在编辑");
        }
        /*
        PictureEditMessageType enumByValue = PictureEditMessageType.getEnumByValue(requestMessage.getType());
        switch (enumByValue) {
            case ENTER_EDIT :
                handleEnterEdit(session, requestMessage);
                break;
            case EDIT_ACTION:
                handleEdit(session, requestMessage);
                break;
            case EXIT_EDIT:
                handleExitEdit(session, requestMessage);
                break;
            default:
                //发送错误消息给其他客户端
                PictureEditResponseMessage message1 = new PictureEditResponseMessage();
                message1.setMessage("未知消息类型");
                message1.setType(PictureEditMessageType.ERROR.getValue());
                UserVO user = (UserVO)session.getAttributes().get("user");
                message1.setUserVO(user);
                broadcastMessage(message1, pictureId);
           }
         */
        //使用Disruptor框架进行生产者消费者模式的消息处理
        //定义事件
        PictureEditEvent event = new PictureEditEvent();
        //事件填充
        event.setPictureEditRequestMessage(requestMessage);
        event.setPictureId(pictureId);
        event.setSession(session);
        event.setUser(user);
        //使用生产者发布事件
        pictureEditEventProducer.publish(event);


    }

    //接下来依次编写每个处理消息的方法。首先是用户进入编辑状态，要设置当前用户为编辑用户，并且向其他客户端发送消息
    public void handleEnterEdit(WebSocketSession session, PictureEditRequestMessage requestMessage) throws IOException {
        if(requestMessage.getType().equals(PictureEditMessageType.ENTER_EDIT.getValue())) {
            UserVO user = (UserVO) session.getAttributes().get("user");
            Long pictureId = (Long) session.getAttributes().get("pictureId");
            //设置当前用户为编辑用户
            boolean b = pictureEditUser.containsKey("pictureId");
            if (!b) {
                pictureEditUser.put(pictureId, user.getId());
            } else {
                return;
            }
            //向其他客户端发送消息
            PictureEditResponseMessage message = new PictureEditResponseMessage();
            message.setMessage(user.getUserName() + "进入了编辑状态");
            message.setType(PictureEditMessageType.ENTER_EDIT.getValue());
            message.setUserVO(user);
            broadcastMessage(null, message, pictureId);
        }

    }


    //用户执行编辑操作时，将该操作同步给 除了当前用户之外 的其他客户端，也就是说编辑操作不用再同步给自己
    public void handleEdit(WebSocketSession session, PictureEditRequestMessage requestMessage) throws IOException {
        Map<String, Object> attributes = session.getAttributes();
        UserVO user = (UserVO)attributes.get("user");
        Long pictureId = (Long)attributes.get("pictureId");
        boolean equals = pictureEditUser.get(pictureId).equals(user.getId());
        if(!equals){

            return;
        }else {
            //向其他用户端发送消息
            PictureEditResponseMessage message = new PictureEditResponseMessage();
            message.setMessage(user.getUserName() + "进行了编辑操作");
            message.setType(PictureEditMessageType. EDIT_ACTION.getValue());
            message.setEditAction(requestMessage.getEditAction());
            message.setUserVO(user);
            broadcastMessage(message, pictureId);

        }
    }


    //用户退出编辑操作时，移除当前用户的编辑状态，并且向其他客户端发送消息
    public void handleExitEdit(WebSocketSession session, PictureEditRequestMessage requestMessage) throws IOException {
        //判断当前用户是否为编辑用户
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        UserVO user = (UserVO) session.getAttributes().get("user");
        boolean equals = pictureEditUser.get(pictureId).equals(user.getId());
        if (!equals) {
            return;
        }
        pictureEditUser.remove(pictureId);
        //向其他用户端发送消息
        PictureEditResponseMessage message = new PictureEditResponseMessage();
        message.setMessage(user.getUserName() + "退出了编辑状态");
        message.setType(PictureEditMessageType.EXIT_EDIT.getValue());
        message.setUserVO(user);
        broadcastMessage(message, pictureId);

    }

    //WebSocket 连接关闭时，需要移除当前用户的编辑状态、并且从集合中删除当前会话，还可以给其他客户端发送消息通知
    public void handleDisconnect(WebSocketSession session,@NotNull CloseStatus status) throws IOException {
        //获取当前用户
        UserVO user = (UserVO) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        //判断当前用户是否处于编辑状态
        boolean equals = pictureEditUser.get(pictureId).equals(user.getId());
        if (equals) {
            //移除当前用户的编辑状态
            pictureEditUser.remove(pictureId);
        }
        // 删除会话
        Set<WebSocketSession> sessionSet = pictureSession.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureSession.remove(pictureId);
            }
        }
        //向其他用户端发送消息
        PictureEditResponseMessage message = new PictureEditResponseMessage();
        message.setMessage(user.getUserName() + "退出了编辑");
        message.setType(PictureEditMessageType.INFO.getValue());
        message.setUserVO(user);
        broadcastMessage(null,message, pictureId);
    }



}
