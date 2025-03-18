package com.cq.YunPhoto.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cq.YunPhoto.Model.dto.SpaceUser.SpaceUserAddRequest;
import com.cq.YunPhoto.Model.dto.SpaceUser.SpaceUserQueryRequest;
import com.cq.YunPhoto.Model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cq.YunPhoto.Model.vo.SpaceUserVo;

import java.util.List;

/**
* @author 86198
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-03-09 15:20:05
*/
public interface SpaceUserService extends IService<SpaceUser> {


    /**
     * 空间用户数据校验
     */
    void checkSpaceUser(SpaceUser spaceUser);

    /**
     * 添加空间用户
     */
    Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 获取单个空间用户封装
     */
    SpaceUserVo getSpaceUserVo(SpaceUser spaceUser);

    /**
     * 获取空间用户列表封装
     */
    List<SpaceUserVo> getSpaceUserList(List<SpaceUser> spaceUserList);

    /**
     * 空间用户查询条件构造
     */
    QueryWrapper<SpaceUser> getSpaceUserWrapper(SpaceUserQueryRequest spaceUserQueryRequest);
}
