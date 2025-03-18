package com.cq.YunPhoto.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cq.YunPhoto.Model.dto.Space.SpaceAddRequest;
import com.cq.YunPhoto.Model.dto.Space.SpaceQueryRequest;
import com.cq.YunPhoto.Model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.vo.Space.SpaceVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86198
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-03-04 13:57:00
*/
public interface SpaceService extends IService<Space> {

    /**
     * 空间数据校验
     */
    void checkSpace(Space space);

    /**
     * 空间数额填充
     */
    void fillSpace(Space space);

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    Long createSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 分页查询
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> queryPageWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取分页查询视图
     * @return
     */
    Page<SpaceVo> querySpaceVoPage(Page<Space> page, HttpServletRequest request);
}
