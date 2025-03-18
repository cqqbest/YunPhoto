package com.cq.YunPhoto.Manager.auth;


import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Model.entity.Picture;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.SpaceUser;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.SpaceTypeEnum;
import com.cq.YunPhoto.Model.enums.SpaceUserRoleEnum;
import com.cq.YunPhoto.constant.spaceUserPermissionConstant;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.service.SpaceUserService;
import com.cq.YunPhoto.service.UserService;
import org.apache.http.client.methods.HttpPatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static cn.hutool.db.meta.TableType.VIEW;
import static com.cq.YunPhoto.constant.UserConstant.USER_LOGIN_STATE;
import static springfox.documentation.spring.web.paths.Paths.contextPath;

@Component
public class stpInterFaceImpl  implements StpInterface {


    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private SpaceUserAuthManger spaceUserAuthManger;
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    ;




    /**
     * 从请求中获取上下文信息
     * @return
     */
    public SpaceUserAuthContext getSpaceUserAuthContext() {

        //获取请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        //获取请求类型（Get/Post）
        String header = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext bean;
        //获取请求参数
        if(ContentType.JSON.getValue().equals(header)){
            //Post请求
            String body = ServletUtil.getBody(request);
            //解析请求体
            bean = JSONUtil.toBean(body, SpaceUserAuthContext.class);

        }else{
            //Get请求
            Map<String,String> params = ServletUtil.getParamMap(request);
            bean = BeanUtil.toBean(params, SpaceUserAuthContext.class);
        }
        //根据请求路径区分id字段的含义
        //获取上下文中的id字段
        Long id = bean.getId();
        if(id != null) {
            //获取请求路径的业务前缀 例如：/api/space/***
            String requestURI = request.getRequestURI();
            //替换上下文，剩下的就是要拿到的部分:space/**
            String replace = requestURI.replace(contextPath + "/", "");
            //获取业务前缀 space
            String string = StrUtil.subBefore(replace, "/", false);//false表示不是最后一个/
            //根据业务前缀判断id字段的含义
            switch (string) {
                case "space":
                    bean.setSpaceId(id);
                    break;
                case "spaceUser":
                    bean.setSpaceUserId(id);
                    break;
                case "picture":
                    bean.setPictureId(id);
                    break;
                default:
                    break;
            }
        }
        return bean;
    }


    /**
     * 权限校验
     *  返回一个账号所拥有的权限码集合
     * @param
     * @param
     * @return
     */
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断 loginType，仅对类型为 "space" 进行权限校验
        if (!StpKit.SPACE.equals(loginType)) {
            return new ArrayList<>();
        }
        // 管理员权限，表示权限校验通过
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManger.getSpaceUserPermission(SpaceUserRoleEnum.ADMIN.getValue());
        // 获取上下文对象
        SpaceUserAuthContext authContext = getSpaceUserAuthContext();
        // 如果所有字段都为空，表示查询公共图库，可以通过
        if (isAllFieldsNull(authContext)) {
            return ADMIN_PERMISSIONS;
        }
        // 获取 userId
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        Long userId = loginUser.getId();
        // 优先从上下文中获取 SpaceUser 对象
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManger.getSpaceUserPermission(spaceUser.getSpaceRole());
        }
        // 如果有 spaceUserId，必然是团队空间，通过数据库查询 SpaceUser 对象
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            spaceUser = spaceUserService.getById(spaceUserId);
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
            }
            // 取出当前登录用户对应的 spaceUser
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (loginSpaceUser == null) {
                return new ArrayList<>();
            }
            // 这里会导致管理员在私有空间没有权限，可以再查一次库处理
            return spaceUserAuthManger.getSpaceUserPermission(loginSpaceUser.getSpaceRole());
        }
        // 如果没有 spaceUserId，尝试通过 spaceId 或 pictureId 获取 Space 对象并处理
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            // 如果没有 spaceId，通过 pictureId 获取 Picture 对象和 Space 对象
            Long pictureId = authContext.getPictureId();
            // 图片 id 也没有，则默认通过权限校验
            if (pictureId == null) {
                return ADMIN_PERMISSIONS;
            }
            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                    .one();
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
            }
            spaceId = picture.getSpaceId();
            // 公共图库，仅本人或管理员可操作
            if (spaceId == null) {
                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    // 不是自己的图片，仅可查看
                    return Collections.singletonList(spaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        // 获取 Space 对象
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
        }
        // 根据 Space 类型判断权限
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getCode()) {
            // 私有空间，仅本人或管理员有权限
            if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            } else {
                return new ArrayList<>();
            }
        } else {
            // 团队空间，查询 SpaceUser 并获取角色和权限
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (spaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManger.getSpaceUserPermission(spaceUser.getSpaceRole());
        }
    }

    /**
     * 判断所有字段为空
     * 反射机制
     * @param object
     * @return
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }



    /**
     * 角色校验，本项目不采用
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     * @param o
     * @param s
     * @return
     */
    @Override
    public List<String> getRoleList(Object o, String s) {
        return Collections.emptyList();
    }
}
