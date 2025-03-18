package com.cq.YunPhoto.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cq.YunPhoto.Model.dto.Space.Analyze.*;
import com.cq.YunPhoto.Model.entity.Picture;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.vo.Space.Analyze.*;
import com.cq.YunPhoto.Model.vo.Space.SpaceVo;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 查询范围校验
     */
    void checkRaUser(spaceAnalyzeRequest request, User loginUser);


    /**
     * 根据权限补充查询条件
     */
    void addConditionByRa(spaceAnalyzeRequest request, QueryWrapper<Picture> wrapper);

    /**
     * 空间资源使用分析
     */
    SpaceUsageAnalyzeResponse spaceUsageAnalyze(SpaceUsageAnalyzeRequest request, User loginUser);

    /**
     * 空间图片分类分析
     */
    List<SpaceCateGoryAnalyzeResponse> spaceCateGoryAnalyze(SpaceCateGoryAnalyzeRequest request, User loginUser);

    /**
     * 空间图片标签分析
     */
    List<SpaceTagAnalyzeResponse> spaceTagAnalyze(SpaceTagAnalyzeRequest request, User loginUser);


    /**
     * 空间图片大小分析
     */
    List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze(SpaceSizeAnalyzeRequest request, User loginUser);

    /**
     * 用户上传行为分析
     */
    List<SpaceUserAnalyzeResponse> spaceUserAnalyze(SpaceUserAnalyzeRequest request, User loginUser);

    /**
     * 空间使用排名分析（管理员使用）
     */
    List<SpaceVo> spaceRankAnalyze(SpaceRankAnalyzeRequest request, User loginUser);
}
