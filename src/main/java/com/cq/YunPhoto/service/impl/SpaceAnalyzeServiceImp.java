package com.cq.YunPhoto.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.dto.Space.Analyze.*;
import com.cq.YunPhoto.Model.entity.Picture;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.vo.Space.Analyze.*;
import com.cq.YunPhoto.Model.vo.Space.SpaceVo;
import com.cq.YunPhoto.mapper.SpaceMapper;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.service.SpaceAnalyzeService;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpaceAnalyzeServiceImp extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;


    /**
     * 权限校验
     * @param request
     * @param loginUser
     */
    @Override
    public void checkRaUser(spaceAnalyzeRequest request ,User loginUser) {
        ThrowUtils.throwIf(request  == null, ErrorCode.NOT_FOUND_ERROR,"请求参数不能为空");
        if(request.getSpaceId() != null ){
            Space space = spaceService.getById(request.getSpaceId());
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NOT_FOUND_ERROR,"没有权限");
            return;
        }
        if(request.isPublic()||request.isAll()){
            Boolean admin = userService.isAdmin(loginUser);
            ThrowUtils.throwIf(!admin, ErrorCode.NOT_FOUND_ERROR,"没有权限");
            return;
        }
        throw  new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误");


    }

    /**
     * 补充查询条件
     * @param request
     * @param wrapper
     */
    @Override
    public void addConditionByRa(spaceAnalyzeRequest request, QueryWrapper<Picture> wrapper) {
        if(ObjUtil.isNotEmpty(request.getSpaceId())){
            wrapper.eq(ObjUtil.isNotEmpty(request.getSpaceId()),"spaceId",request.getSpaceId());
            return;
        }
        if(request.isPublic()){
            wrapper.isNull("spaceId");
            return;
        }
        if(request.isAll()){
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误");
    }

    /**
     * 空间资源使用分析
     * @param request
     * @param loginUser
     * @return
     */
    @Override
    public SpaceUsageAnalyzeResponse spaceUsageAnalyze(SpaceUsageAnalyzeRequest request, User loginUser) {
        //权限校验
        checkRaUser(request,loginUser);
        //查询所有图库或公共图库
        if(request.isAll()||request.isPublic()){
            QueryWrapper<Picture> wrapper = new QueryWrapper<>();
            wrapper.select("picSize");
            if(request.isPublic()){
                wrapper.isNull("spaceId");
            }
            List<Picture> pictures = pictureService.list(wrapper);
            //计算总大小
            Long totalSize = pictures.stream().mapToLong(Picture::getPicSize).sum();
            //获取图片数量
            Long totalCount = (long) pictures.size();
            //封装结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            //公共图库和总图库大小和数量没有上限
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setUsedSize(totalSize);
            spaceUsageAnalyzeResponse.setUsedCount(totalCount);
            return spaceUsageAnalyzeResponse;
        }else {
            //查询用户空间
            Space space = spaceService.getById(request.getSpaceId());
            //封装结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalCount());
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            //计算占比
            double sizeB = NumberUtil.round(spaceUsageAnalyzeResponse.getUsedSize() * 1.0 / spaceUsageAnalyzeResponse.getMaxSize() * 100, 2).doubleValue();
            final double countB = NumberUtil.round(spaceUsageAnalyzeResponse.getUsedCount() * 1.0 / spaceUsageAnalyzeResponse.getMaxCount() * 100, 2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeB);
            spaceUsageAnalyzeResponse.setCountUsageRatio(countB);
            return spaceUsageAnalyzeResponse;
        }


    }

    /**
     * 空间图片分类分析
     * @param request
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceCateGoryAnalyzeResponse> spaceCateGoryAnalyze(SpaceCateGoryAnalyzeRequest request, User loginUser) {
        //权限校验
        checkRaUser(request,loginUser);
        //构造查询条件
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        addConditionByRa(request,wrapper);
        wrapper.select("picSize");
        List<SpaceCateGoryAnalyzeResponse> list = new ArrayList<>();
        pictureService.list(wrapper).stream().collect(Collectors.groupingBy(Picture::getCategory)).forEach(((k,v)->{
            SpaceCateGoryAnalyzeResponse spaceCateGoryAnalyzeResponse = new SpaceCateGoryAnalyzeResponse();
            if(k == null){
                k = "未分类";
            }
            spaceCateGoryAnalyzeResponse.setCategory(k);
            spaceCateGoryAnalyzeResponse.setCount((long) v.size());
            spaceCateGoryAnalyzeResponse.setSize(v.stream().mapToLong(Picture::getPicSize).sum());
            list.add(spaceCateGoryAnalyzeResponse);
        }));
        return list;
    }

    /**
     * 空间图片标签分析
     * @param request
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceTagAnalyzeResponse> spaceTagAnalyze(SpaceTagAnalyzeRequest request, User loginUser) {
        //权限校验
        checkRaUser(request, loginUser);
        //构造查询条件
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        wrapper.select("tags");
        addConditionByRa(request, wrapper);
        List<SpaceTagAnalyzeResponse> list = new ArrayList<>();
        pictureService.getBaseMapper().selectObjs(wrapper).stream().collect(Collectors.groupingBy(Object::toString)).forEach(((k, v) -> {
            SpaceTagAnalyzeResponse spaceTagAnalyzeResponse = new SpaceTagAnalyzeResponse();
            if (k == null) {
                k = "未设标签";
            }
            spaceTagAnalyzeResponse.setTagName(k);
            spaceTagAnalyzeResponse.setTagCount(v.size());
            list.add(spaceTagAnalyzeResponse);
        }));
        //排序
        list.sort(Comparator.comparingLong(SpaceTagAnalyzeResponse::getTagCount).reversed());
        return list;
    }


    /**
     * 空间图片大小分析
     * @param request
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze(SpaceSizeAnalyzeRequest request, User loginUser) {
        //权限校验
        checkRaUser(request, loginUser);
        //构造查询条件
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        addConditionByRa(request, wrapper);
        wrapper.select("picSize");
        //查询数据库
        final List<Long> collect1 = pictureService.getBaseMapper().selectObjs(wrapper).stream().map(o -> (Long) o).collect(Collectors.toList());
        Map<String,Long> map = new HashMap<>();
        map.put("<2KB",collect1.stream().filter(o->o<2*1024).count());
        map.put("2KB-10KB",collect1.stream().filter(o->o>=2*1024&&o<10*1024).count());
        map.put("10KB-100KB",collect1.stream().filter(o->o>=10*1024&&o<100*1024).count());
        map.put("100KB-1MB",collect1.stream().filter(o->o>=100*1024&&o<1024*1024).count());
        map.put(">1MB",collect1.stream().filter(o->o>=1024*1024).count());
        List<SpaceSizeAnalyzeResponse> list = new ArrayList<>();

        map.forEach((k,v)->{
            SpaceSizeAnalyzeResponse spaceSizeAnalyzeResponse = new SpaceSizeAnalyzeResponse();
            spaceSizeAnalyzeResponse.setSizeRange(k);
            spaceSizeAnalyzeResponse.setCount(v);
            list.add(spaceSizeAnalyzeResponse);
        });
        return list;
    }

    @Override
    public List<SpaceUserAnalyzeResponse> spaceUserAnalyze(SpaceUserAnalyzeRequest request, User loginUser) {
        //权限校验
        checkRaUser(request, loginUser);
        //构造查询条件
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        addConditionByRa(request, wrapper);
        wrapper.eq("userId", request.getUserId());
        if(request.getTimeDimension() == "day"){
            wrapper.select("DATE_FORMAT(createTime,'%Y-%m-%d') as createTime", "count(*) as count");
            wrapper.groupBy("createTime");
            wrapper.orderByAsc("createTime");
        } else if (request.getTimeDimension() == "month") {
            wrapper.select("DATE_FORMAT(createTime,'%Y-%m') as createTime", "count(*) as count");
            wrapper.groupBy("createTime");
            wrapper.orderByAsc("createTime");

        } else if (request.getTimeDimension() == "week") {
            wrapper.select("YEARWEEK(createTime) as createTime", "count(*) as count");
            wrapper.groupBy("createTime");
            wrapper.orderByAsc("createTime");
        }else {
            throw new RuntimeException("时间维度错误");
        }
        //查询数据库
        return pictureService.getBaseMapper()
                .selectObjs(wrapper)
                .stream()
                .map(o -> (SpaceUserAnalyzeResponse) o)
                .collect(Collectors.toList());
    }

    /**
     * 空间使用排行分析（管理员使用）
     * @param request
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceVo> spaceRankAnalyze(SpaceRankAnalyzeRequest request, User loginUser) {
        //权限校验
        Boolean admin = userService.isAdmin(loginUser);
        ThrowUtils.throwIf(!admin, ErrorCode.OPERATION_ERROR,"权限不足");
        //构造查询条件
        QueryWrapper<Space> wrapper = new QueryWrapper<>();
        wrapper.select("spaceName","totalSize","id","userId");
        wrapper.last("Limit "+request.getTopN());//取前n名
        //查询数据库
        List<Space> spaceVos = spaceService.list(wrapper);
        List<SpaceVo> collect = spaceVos.stream().map(SpaceVo::toSpaceVo).collect(Collectors.toList());
        return collect;
    }


}
