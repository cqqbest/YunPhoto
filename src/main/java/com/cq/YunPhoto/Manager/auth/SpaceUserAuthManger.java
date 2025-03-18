package com.cq.YunPhoto.Manager.auth;


import cn.hutool.core.io.IoUtil;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Manager.auth.model.spaceUserAuthConfig;
import com.cq.YunPhoto.Manager.auth.model.spaceUserPermission;
import com.cq.YunPhoto.Manager.auth.model.spaceUserRole;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.SpaceUser;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.SpaceTypeEnum;
import com.cq.YunPhoto.Model.enums.SpaceUserRoleEnum;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.service.SpaceUserService;
import com.cq.YunPhoto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpaceUserAuthManger {


    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    public static spaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    /**
     * 获取空间用户权限配置
     * @throws IOException
     */
    static {
        //从环境中获取配置文件
        ClassPathResource classPathResource = new ClassPathResource("biz/SpaceUserAuthConfig.json");
        String config = null;
        try {
            config = IoUtil.read(classPathResource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取空间用户权限配置失败");
        }
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(config, spaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     */
    public    List<String> getSpaceUserPermission(String spaceUserRole) {
        final List<spaceUserRole> role = SPACE_USER_AUTH_CONFIG.getRole();
        for (spaceUserRole role1 : role) {
            if (role1.getKey().equals(spaceUserRole)) {
                return role1.getPermission();
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户角色错误");
    }


    /**
     * 获取权限列表
     */
    public   List<String> getSpaceUserPermissionList(Space space, User loginUser) {
        //设置全部权限
        List<String> ALL_PERMISSION = getSpaceUserPermission(SpaceUserRoleEnum.ADMIN.getValue());

        //判断是否登录
        if(loginUser == null){
            return new ArrayList<>();
        }
        //判断是用户空间还是公共图库
        if(space == null){
            //公共图库，如果是管理员，则有全部权限
            if(userService.isAdmin(loginUser)){
                return ALL_PERMISSION;
            }
            //不是管理员则只有浏览权限
            return getSpaceUserPermission(SpaceUserRoleEnum.VIEWER.getValue());
        }
        //如果是用户空间判断是私有空间还是团队空间
        if(space.getSpaceType().equals(SpaceTypeEnum.PRIVATE.getCode())){
            //私有空间,如果用户是空间创始人或是系统管理员有全部权限
            if(space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)){
                return ALL_PERMISSION;
            }
        }
        //团队空间
        if(space.getSpaceType().equals(SpaceTypeEnum.TEAM.getCode())){
            //团队空间
            //判断用户是否是团队空间成员
            SpaceUser one = spaceUserService
                    .getOne(new QueryWrapper<SpaceUser>()
                            .eq("userId", loginUser.getId())
                            .eq("spaceId", space.getId())
                    );
            if(one != null){
                //是团队空间成员，获取用户角色
                String spaceUserRole = one.getSpaceRole();
                //获取权限列表
                return getSpaceUserPermission(spaceUserRole);
            }
        }
        return new ArrayList<>();

    }
}
