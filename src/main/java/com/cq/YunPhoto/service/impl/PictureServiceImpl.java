package com.cq.YunPhoto.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cq.YunPhoto.Api.Aliyun.AliyunAiApi;
import com.cq.YunPhoto.Api.Aliyun.model.CreateOutPaintingTaskRequest;
import com.cq.YunPhoto.Api.Aliyun.model.CreateOutPaintingTaskResponse;
import com.cq.YunPhoto.Api.Aliyun.model.GetOutPaintingTaskResponse;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Manager.CosManager;
import com.cq.YunPhoto.Manager.auth.SpaceUserAuthManger;
import com.cq.YunPhoto.Manager.upLoad.FilePictureUpLoad;
import com.cq.YunPhoto.Manager.upLoad.PictureUpLoadTemplate;
import com.cq.YunPhoto.Manager.upLoad.UrlPictureUpLoad;
import com.cq.YunPhoto.Model.dto.File.UpLoadPictureResult;
import com.cq.YunPhoto.Model.dto.Picture.*;
import com.cq.YunPhoto.Model.entity.Picture;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.PictureReviewStatusEnum;
import com.cq.YunPhoto.Model.vo.PictureVo;
import com.cq.YunPhoto.Model.vo.UserVO;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.mapper.PictureMapper;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.service.UserService;
import com.cq.YunPhoto.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
* @author 86198
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-02-27 16:47:56
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService{

    //弃用
    //@Resource
    //private FileManager fileManager;
    //会导致报错找到两个符合条件的Bean
    //@Resource
    //private PictureUpLoadTemplate pictureUpLoadTemplate;
    @Resource
    private FilePictureUpLoad filePictureUpLoad;
    @Resource
    private UrlPictureUpLoad urlPictureUpLoad;
    //循环依赖
    //@Resource
    //private PictureService pictureService;
    @Resource
    private UserService userService;
    @Autowired
    private CosManager cosManager;

    @Resource
    private SpaceService spaceService;

    @Resource//开启事物
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserAuthManger spaceUserAuthManger;

    @Override
    public PictureVo uploadPicture(User userInfo, Object inputSource, PictureUploadRequest pictureUploadRequest) {
        //判断用户是否存在
        ThrowUtils.throwIf(userInfo == null, ErrorCode.NOT_FOUND_ERROR);
        //判断是更新图片还是第一次上传该id图片
        Long pictureId = null;
        if(pictureUploadRequest != null){
            pictureId = pictureUploadRequest.getId();
        }
        //判断是否为当前空间的创始人
        Long spaceId = pictureUploadRequest.getSpaceId();
        if(spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            boolean equals = space.getUserId().equals(userInfo.getId());
            ThrowUtils.throwIf(!equals, ErrorCode.SYSTEM_ERROR, "没有空间权限");
            //判断容量是否足够
            Long maxCount = space.getMaxCount();
            Long maxSize = space.getMaxSize();
            Long totalCount = space.getTotalCount();
            Long totalSize = space.getTotalSize();
            ThrowUtils.throwIf(totalCount >= maxCount, ErrorCode.SYSTEM_ERROR, "图片数量已达上限");
            ThrowUtils.throwIf(totalSize >= maxSize, ErrorCode.SYSTEM_ERROR, "空间容量已满");
        }
        //pictureId不为空表示更新图片
        if(pictureId != null){
            //判断图片是否存在
            Picture picture = this.getById(pictureId);
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //权限判断，是否为当前图片本人更新还是管理员更新
            ThrowUtils.throwIf(!picture.getUserId().equals(userInfo.getId()) && !userService.isAdmin(userInfo), ErrorCode.SYSTEM_ERROR, "权限不足");
            //判断图片的SpaceId和传递的是否一致
            Long oldSpaceId = picture.getSpaceId();
            if(spaceId != null) {
                ThrowUtils.throwIf(!oldSpaceId.equals(spaceId), ErrorCode.SYSTEM_ERROR, "图片不属于该空间");
            }
        }
        //构造文件前缀(有空间id根据空间id划分，没有则根据用户id划分)
        String prefix = null;
        if(spaceId == null) {
            prefix = StrUtil.format("public/%s", userInfo.getId());
        } else{
            prefix = StrUtil.format("space/%s", spaceId);
        }
        //上传图片
        //UpLoadPictureResult upLoadPictureResult = fileManager.uploadPicture(multipartFile, prefix);
        //根据上传方式选择上传模板
        PictureUpLoadTemplate pictureUpLoadTemplate = filePictureUpLoad;
        if(inputSource instanceof String){
            pictureUpLoadTemplate = urlPictureUpLoad;
        }
        //更新，删除cos旧图(自己扩展的)
        if(pictureId != null){
            //删除cos旧图
            String fileUrl = pictureUploadRequest.getUrl();
            //获取key
            String key = fileUrl.substring(fileUrl.lastIndexOf("/")+1);
            cosManager.deleteFile(key);
        }
        UpLoadPictureResult upLoadPictureResult = pictureUpLoadTemplate.uploadPicture(inputSource, prefix);
        //保存图片信息
        Picture picture = new Picture();
        BeanUtil.copyProperties(upLoadPictureResult, picture);

        String picName = pictureUploadRequest.getPicName();
        if(StrUtil.isNotBlank(picName)&&upLoadPictureResult != null){
            picture.setName(picName);
        }
        picture.setUserId(userInfo.getId());
        //设置图片审核状态
        this.setPictureReviewStatus(picture, userInfo);
        //如果是更新图片则还保留原图片id和空间id
        if(pictureId != null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
            picture.setSpaceId(spaceId);
        }
        //保存图片事物，控制图片额度
        final long  finalSpaceId = spaceId;
        transactionTemplate.execute(status->{
            //保存图片
            boolean save = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存图片失败");
            if(ObjUtil.isNotEmpty(finalSpaceId)) {
                //更新用户空间图片数量和图片大小
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("TotalCount = TotalCount + 1")
                        .setSql("TotalSize = TotalSize + " + picture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "更新空间图片数量失败");
            }
            return picture;
        });
        PictureVo pictureVo = new PictureVo();
        pictureVo.toPictureVo(picture);
        return pictureVo;
    }

    /**
     * 获取脱敏图片
     * @param picture
     * @param
     * @return
     */
    @Override
    public PictureVo getPictureVo(Picture picture) {
        //判断图片是否存在
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //获取图片关联用户
        User user = userService.getById(picture.getUserId());
        //判断用户是否存在
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        PictureVo pictureVo = new PictureVo();
        pictureVo.toPictureVo(picture);
        UserVO userVO = userService.getUserVO(user);
        pictureVo.setUser(userVO);
        return pictureVo;
    }

    @Override
    public QueryWrapper<Picture> queryPageWrapper(PictureQueryRequest pictureQueryRequest) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.SYSTEM_ERROR, "请求参数为空");
        //获取参数
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        Long userId = pictureQueryRequest.getUserId();
        String category = pictureQueryRequest.getCategory();
        String picFormat = pictureQueryRequest.getPicFormat();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        List<String> tags = pictureQueryRequest.getTags();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        String searchText = pictureQueryRequest.getSearchText();
        String order = pictureQueryRequest.getOrder();
        String sort = pictureQueryRequest.getSort();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Boolean isSpaceNull = pictureQueryRequest.getIsSpaceNull();
        String endEditTime = pictureQueryRequest.getEndEditTime();
        String startEditTime = pictureQueryRequest.getStartEditTime();
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isEmpty(name), "name", name);
        queryWrapper.eq(ObjUtil.isEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isEmpty(category), "category", category);
        queryWrapper.eq(ObjUtil.isEmpty(picFormat), "picFormat", picFormat);
        queryWrapper.eq(ObjUtil.isEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.orderBy(ObjUtil.isEmpty(order),order.equals("descend"),order);
        queryWrapper.orderByAsc(ObjUtil.isEmpty(sort), sort);
        queryWrapper.like(ObjUtil.isEmpty(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjUtil.isEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(ObjUtil.isNotEmpty(searchText), "name", searchText);
        queryWrapper.like(ObjUtil.isNotEmpty(introduction), "introduction",introduction);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(ObjUtil.isNotEmpty(isSpaceNull), "spaceId");
        queryWrapper.eq(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
        queryWrapper.eq(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        if(ObjUtil.isNotEmpty(searchText)){
            queryWrapper.and(wrapper -> wrapper.like("name", searchText).or().like("introduction", searchText));
        }
        if(CollUtil.isNotEmpty(tags)){
            for (String tag : tags) {
                queryWrapper.like("tags","\""+tag+"\"");
            }
        }
        return queryWrapper;
    }


    /**
     * 分页获取图片封装
     * @param page
     * @param request
     * @return
     */
    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVo> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVo> pictureVOList = pictureList.stream().map(PictureVo::toPictureVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    /**
     * 图片数据校验
     * @param picture
     */

    @Override
    public void checkPicture(Picture picture) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(picture.getName()), ErrorCode.NOT_FOUND_ERROR,"图片名称不能为空");
        ThrowUtils.throwIf(ObjUtil.isEmpty(picture.getPicFormat()), ErrorCode.NOT_FOUND_ERROR,"图片格式不能为空");
        ThrowUtils.throwIf(ObjUtil.isEmpty(picture.getId()), ErrorCode.NOT_FOUND_ERROR,"Id不能为空");
        ThrowUtils.throwIf(picture.getUrl().length()>1024,ErrorCode.PARAMS_ERROR,"图片url太长");
        if(StrUtil.isNotBlank(picture.getIntroduction())){
            ThrowUtils.throwIf(picture.getIntroduction().length()>50,ErrorCode.PARAMS_ERROR,"图片简介太长");
        }
    }

    /**
     * 管理员图片审核
     * @param pictureReviewRequest
     * @param loginUser
     */
    @Override
    public void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //判断请求是否为空
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureReviewRequest), ErrorCode.NOT_FOUND_ERROR,"请求不能为空");
        //判断审核请求是否合法
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String messageByCode = PictureReviewStatusEnum.getMessageByCode(reviewStatus);
        ThrowUtils.throwIf(ObjUtil.isEmpty(messageByCode), ErrorCode.NOT_FOUND_ERROR,"审核状态不合法");
        //判断图片是否存在
        Picture picture = this.getById(pictureReviewRequest.getId());
        ThrowUtils.throwIf(ObjUtil.isEmpty(picture), ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        //判断图片是否已经审核
        ThrowUtils.throwIf(picture.getReviewStatus().equals(reviewStatus), ErrorCode.NOT_FOUND_ERROR,"图片已经审核");
        //修改图片审核状态
        picture.setReviewStatus(reviewStatus);
        picture.setReviewMessage(pictureReviewRequest.getReviewMessage());
        //填充审核人信息
        picture.setReviewerId(loginUser.getId());
        //填充审核时间
        picture.setReviewTime(new Date());
        boolean b = this.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.NOT_FOUND_ERROR,"图片审核失败");
    }


    /**
     * 设置图片审核状态
     * @param picture
     * @param loginUser
     */
    @Override
    public void setPictureReviewStatus(Picture picture, User loginUser) {
        //判断用户是否为管理员
        Boolean admin = userService.isAdmin(loginUser);
        //如果是管理员，直接审核通过
        if(admin){
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getCode());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
        }
        //如果不是管理员，设置待审核状态
        picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getCode());
    }

    /**
     * 批量抓取和创建图片
     * @param pictureUpLoadByBatchRequest
     * @param loginUser
     * @return
     */
    @Override
    public Integer uploadPictureByBatch(PictureUpLoadByBatchRequest pictureUpLoadByBatchRequest, User loginUser) {
        String searchText = pictureUpLoadByBatchRequest.getSearchText();
        Integer count = pictureUpLoadByBatchRequest.getCount();
        //判断搜索词是否为空
        ThrowUtils.throwIf(ObjUtil.isEmpty(searchText), ErrorCode.NOT_FOUND_ERROR,"搜索词不能为空");
        //判断抓取数量是否大于限定数，假设限制为30
        ThrowUtils.throwIf(count>30, ErrorCode.NOT_FOUND_ERROR,"抓取数量不能大于30");
        //设置抓取地址
        String url = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1",searchText);
        //设置请求头
        Document document;
        try {
            //利用jsoup进行抓取
            document = Jsoup.connect(url).get();
        } catch (IOException e) {

            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图片抓取失败");
        }
        //获取图片元素
        Element elements = document.getElementsByClass("dgControl").first();
        if(ObjUtil.isEmpty(elements)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图片抓取失败");
        }
        //获取图片地址
        Elements select = elements.select("img.mimg");
        int loadCount = 0;
        //遍历图片地址
        for (Element element : select) {
            String fileUrl = element.attr("src");
            if(ObjUtil.isEmpty(fileUrl)){
                log.info("当前连接为空,已跳过,{}",fileUrl);
                continue;
            }
            //处理图片上传地址，防止转义
            int i = fileUrl.indexOf("?");
            if(i>-1){
                fileUrl = fileUrl.substring(0,i);
            }
            //上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            //设置图片名称
            String namePrefix = pictureUpLoadByBatchRequest.getNamePrefix();
            if(StrUtil.isBlank(namePrefix)){
                //如果请求中没传。默认为搜索词
                namePrefix = pictureUpLoadByBatchRequest.getSearchText();
            }
            pictureUploadRequest.setPicName(namePrefix);
            try {
                this.uploadPicture(loginUser, fileUrl, pictureUploadRequest);
                loadCount++;
            }catch (Exception e){
                log.info("当前连接上传失败,已跳过,{}",fileUrl);
                continue;
            }
            if(loadCount>=count){
                break;
            }

        }
        return loadCount;

    }

    /**
     * 从cos删除图片
     * @param picture
     */
    @Async//该注解使方法被异步调用
    @Override
    public void deletePictureFromCos(Picture picture) {
        String url = picture.getUrl();
        //判断图片是否被引用
        Long count = this.lambdaQuery().eq(Picture::getUrl, url).count();
        if(count>0){
            return;
        }
        //根据url获取文件key
        String key = url.substring(url.lastIndexOf("/")+1);
        //删除图片
        cosManager.deleteFile(key);
        //删除缩略图
        String thumbnailUrl = picture.getThumbnailUrl();
        if(StrUtil.isNotBlank(thumbnailUrl)){
            String thumbnailKey = thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/")+1);
            cosManager.deleteFile(thumbnailKey);
        }
    }

    /**
     * 图片校验(编辑图片和更新图片时用)
     * @param picture
     * @param loginUser
     */
    @Override
    public void checkPictureSpace(Picture picture, User loginUser) {
        Long spaceId = picture.getSpaceId();
        //图片空间id为空表示公共图库，管理员和图片创始人可以操作
        if(ObjUtil.isEmpty(spaceId)){
            ThrowUtils.throwIf(!picture.getUserId().equals(loginUser.getId())&&!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR,"无权限操作");
        }
        //图片空间id不为空，表示私有图库，只有图片创始人可以操作
        else {
            ThrowUtils.throwIf(!picture.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR,"无权限操作");
        }
    }

    /**
     * 根据图片主色调查询用户空间内的图片列表
     * @param SpaceId
     * @param color
     * @param userLogin
     * @return
     */
    @Override
    public List<PictureVo> getSimilarPictureList(long SpaceId,String color,User userLogin) {
        //权限校验
        Space space = spaceService.getById(SpaceId);
        ThrowUtils.throwIf(!space.getUserId().equals(userLogin.getId()), ErrorCode.NO_AUTH_ERROR,"无权限操作");
        //查询空间内所有有主色调的图片
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, SpaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        //判空，没有返回空列表
        if(CollUtil.isEmpty(pictureList)){
            return new ArrayList<>();
        }
        //获取主色调为指定颜色的图片
        List<Picture> collect = pictureList.stream().sorted(Comparator.comparingDouble(picture->{
            String picColor = picture.getPicColor();
            if(StrUtil.isBlank(picColor)){
                return Double.MAX_VALUE;
            }
            double v = ColorSimilarUtils.calculateSimilarity(color, picColor);
            return -v;}))
                .limit(12)
                .collect(Collectors.toList());
        //获取封装列表
        List<PictureVo> voCollect = collect.stream()
                .map(picture -> {
                    PictureVo pictureVo = BeanUtil.copyProperties(picture, PictureVo.class);
                    pictureVo.setUser(userService.getUserVO(userService.getById(picture.getUserId())));
                    //获取权限列表，自己添加
                    pictureVo.setPermissionList(spaceUserAuthManger.getSpaceUserPermissionList(spaceService.getById(picture.getSpaceId()),userService.getById(picture.getUserId())));
                    return pictureVo;
                        }
                )
                .collect(Collectors.toList());
        return voCollect;
    }

    @Override
    @Transactional//事务
    public void updateBatchPicture(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        //判断请求是否为空
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureEditByBatchRequest), ErrorCode.PARAMS_ERROR);
        //权限校验
        String spaceId = pictureEditByBatchRequest.getSpaceId();
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(ObjUtil.isEmpty(space),ErrorCode.NOT_FOUND_ERROR,"用户空间不存在");
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR,"无权限操作");
        //获取图片列表
        List<Picture> list = this.lambdaQuery()
                .select(Picture::getSpaceId,Picture::getId)//表示查询结果中只包含spaceId和id两个字段，减少查询不必要字段浪费时间
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureEditByBatchRequest.getPictureIds())
                .list();
        //判断图片列表是否为空
        ThrowUtils.throwIf(CollUtil.isEmpty(list), ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        //更新图片
        list.forEach(picture -> {
            if(ObjUtil.isNotEmpty(pictureEditByBatchRequest.getCategory())){
                picture.setCategory(pictureEditByBatchRequest.getCategory());
            }
            if(CollUtil.isNotEmpty(pictureEditByBatchRequest.getTags())){
                this.lambdaUpdate()
                        .set(Picture::getTags, pictureEditByBatchRequest.getTags().toString())
                        .eq(Picture::getId, picture.getId())
                        .update();
            }
        });

        //批量重命名
        //获取重命名规则
        String namingRule = pictureEditByBatchRequest.getNamingRule();
        int count = 1;
        try{
            for(Picture picture : list){
                if(StrUtil.isNotBlank(namingRule)) {
                    //利用正则表达式replaceAll方法将{序号}替换为count
                    picture.setName(namingRule.replaceAll("\\{序号}",String.valueOf(count++)));
                }
            }
        }catch (Exception e){
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR,"批量重命名失败");
        }

        //批量修改
        boolean b = this.updateBatchById(list);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR,"修改失败");
    }

    /**
     * 创建Ai扩图请求
     */
    @Override
    public CreateOutPaintingTaskResponse PictureOutPainting(CreatPictureOutPaintingRequest creatPictureOutPaintingRequest, User loginUser) {
        //判断请求是否为空
        ThrowUtils.throwIf(ObjUtil.isEmpty(creatPictureOutPaintingRequest), ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        ThrowUtils.throwIf(ObjUtil.isEmpty(creatPictureOutPaintingRequest.getPictureId()), ErrorCode.PARAMS_ERROR,"图片id不能为空");
        //获取图片
        Picture picture = this.getById(creatPictureOutPaintingRequest.getPictureId());
        ThrowUtils.throwIf(ObjUtil.isEmpty(picture), ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        //权限校验
        Long spaceId = picture.getSpaceId();
        if(spaceId != null){
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isEmpty(space),ErrorCode.NOT_FOUND_ERROR,"用户空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR,"无权限操作");
        }
        //设置请求信息
        CreateOutPaintingTaskRequest request = new CreateOutPaintingTaskRequest();
        request.setParameters(creatPictureOutPaintingRequest.getParameters());
        request.getInput().setImageUrl(picture.getUrl());
        //调用Api
        AliyunAiApi aliyunAiApi = new AliyunAiApi();
        return aliyunAiApi.CreateOutPaintingTask(request);
    }
}




