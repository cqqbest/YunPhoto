package com.cq.YunPhoto.Manager.WebSocket;

import com.cq.YunPhoto.Manager.auth.SpaceUserAuthManger;
import com.cq.YunPhoto.Model.entity.Picture;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.SpaceTypeEnum;
import com.cq.YunPhoto.constant.spaceUserPermissionConstant;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.service.UserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * WebSocket握手拦截器
 */

@Component
public class WSHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManger spaceUserAuthManger;




    /**
     * 前置拦截器
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        HttpServletRequest httpServletRequest = null;
        if(request instanceof HttpServletRequest){
            httpServletRequest = (HttpServletRequest) request;
        }else {
            return false;
        }
        Long pictureId = (Long)httpServletRequest.getSession().getAttribute("pictureId");
        if(pictureId == null){
            return false;
        }
        //判断图片所在空间是否为团队空间
        Picture picture = pictureService.getById(pictureId);
        if(picture == null){
            return false;
        }
        Long spaceId = picture.getSpaceId();
        if(spaceId == null){
            return false;
        }
        Space space = spaceService.getById(spaceId);
        if(space == null){
            return false;
        }
        if(!space.getSpaceType().equals(SpaceTypeEnum.TEAM.getCode())){
            return false;
        }
        //判断用户是否具有编辑权限
        User loginUser = userService.getLoginUser(httpServletRequest);
        if(loginUser == null){
            return false;
        }
        List<String> spaceUserPermissionList = spaceUserAuthManger.getSpaceUserPermissionList(space, loginUser);
        if(!spaceUserPermissionList.contains(spaceUserPermissionConstant.PICTURE_EDIT) && !spaceUserPermissionList.contains(spaceUserPermissionConstant.SPACE_USER_MANGER)){
            return false;
        }

        attributes.put("pictureId",pictureId);
        attributes.put("userId",loginUser.getId());
        attributes.put("user",loginUser);
        return true;
    }


    /**
     * 用户退出，断开连接
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {


    }
}
