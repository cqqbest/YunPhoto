package com.cq.YunPhoto.Controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cq.YunPhoto.Annotation.AuthCheck;
import com.cq.YunPhoto.Api.Aliyun.AliyunAiApi;
import com.cq.YunPhoto.Api.Aliyun.model.CreateOutPaintingTaskResponse;
import com.cq.YunPhoto.Api.Aliyun.model.GetOutPaintingTaskResponse;
import com.cq.YunPhoto.Api.ImageSearch.imageSearchApiFacade;
import com.cq.YunPhoto.Api.ImageSearch.model.imageSearchResult;
import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.DeleteRequest;
import com.cq.YunPhoto.Common.ResultUtils;
import com.cq.YunPhoto.Config.RedissonConfig;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Manager.auth.SpaceUserAuthContext;
import com.cq.YunPhoto.Manager.auth.SpaceUserAuthManger;
import com.cq.YunPhoto.Manager.auth.StpKit;
import com.cq.YunPhoto.Manager.auth.annotation.SaSpaceCheckPermission;
import com.cq.YunPhoto.Model.dto.Picture.*;
import com.cq.YunPhoto.Model.entity.Picture;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.enums.PictureReviewStatusEnum;
import com.cq.YunPhoto.Model.vo.PictureTagCategory;
import com.cq.YunPhoto.Model.vo.PictureVo;
import com.cq.YunPhoto.Model.vo.UserVO;
import com.cq.YunPhoto.constant.UserConstant;
import com.cq.YunPhoto.constant.spaceUserPermissionConstant;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.service.SpaceService;
import com.cq.YunPhoto.service.UserService;
//import com.github.benmanes.caffeine.cache.Cache;
//import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private SpaceService spaceService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private SpaceUserAuthManger spaceUserAuthManger;

    //上传图片

    /**
     * 上传文件类型的图片
     * @param multipartFile
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVo> uploadPicture(@RequestParam("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //获取用户
        User userInfo = userService.getLoginUser(request);
        //调用service层方法
        PictureVo pictureVo = pictureService.uploadPicture(userInfo, multipartFile, pictureUploadRequest);
        return ResultUtils.success(pictureVo);

    }

    /**
     * 上传url类型的图片
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVo> uploadPictureUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //获取用户
        User userInfo = userService.getLoginUser(request);
        //获取url地址
        String url = pictureUploadRequest.getUrl();
        //调用service层方法
        PictureVo pictureVo = pictureService.uploadPicture(userInfo,url,pictureUploadRequest);
        return ResultUtils.success(pictureVo);
    }

    //图片管理
    /**
    *根据id删除图片(管理员和本人可以操作,私人空间只有本人可以操作)
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //判读图片是否存在
        Picture oldPicture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //身份校验(使用了Sa-token，不在需要自己进行身份验证)
        //pictureService.checkPictureSpace(oldPicture, userService.getLoginUser(request));
        //开启一个事物控制删除和更新操作的原子性
        Space space = spaceService.getById(oldPicture.getSpaceId());
        final long finalSpaceId = space.getId();
        transactionTemplate.execute(status -> {
            //删除照片
            boolean b = pictureService.removeById(deleteRequest.getId());
            //判断是否删除成功
            ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "删除失败");
            //更新私有空间图片数量和空间大小
            if(ObjUtil.isNotEmpty(finalSpaceId)){
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("TotalCount=TotalCount-1")
                        .setSql("TotalSize=TotalSize-" + oldPicture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "更新失败");
            }
            return true;
        });
        //异步从cos删除图片(自己加的)
        pictureService.deletePictureFromCos(oldPicture);
        return ResultUtils.success(true);

    }

    /**
     * 管理员获取图片，不需要脱敏
     */
    @GetMapping("/get")
    @AuthCheck(UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPicture(@RequestParam("id") Long id, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR);
        //判读图片是否存在
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(picture);
    }
    /**
    *获取图片（脱敏后的）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVo> getUserPicture(@RequestParam("id") Long id, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR);
        //判读图片是否存在
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //判断图片是否审核通过
        ThrowUtils.throwIf(!picture.getReviewStatus().equals(PictureReviewStatusEnum.PASS.getCode()), ErrorCode.NOT_FOUND_ERROR, "图片未审核通过");
        //判断是否为私人空间的照片
        Long spaceId = picture.getSpaceId();
        if(spaceId != null){
            //User loginUser = userService.getLoginUser(request);
            //pictureService.checkPictureSpace(picture, loginUser);
            //使用Sa-Token编程式身份验证
            boolean b = StpKit.SPACE.hasPermission(spaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!b,ErrorCode.NO_AUTH_ERROR, "没有权限");
        }
        //获取脱敏后的图片
        PictureVo pictureVo = pictureService.getPictureVo(picture);
        User loginUser = userService.getLoginUser(request);
        UserVO userVO = userService.getUserVO(loginUser);
        pictureVo.setUser(userVO);
        //获取权限列表
        Space space = spaceService.getById(spaceId);
        pictureVo.setPermissionList(spaceUserAuthManger.getSpaceUserPermissionList(space, loginUser));
        return ResultUtils.success(pictureVo);
    }

    /**
     *分页获取图片列表（管理员用，不用脱敏）
     */
    @GetMapping("/list")
    @AuthCheck(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicture(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //获取图片列表
        QueryWrapper<Picture> pictureQueryWrapper = pictureService.queryPageWrapper(pictureQueryRequest);
        long pageSize = pictureQueryRequest.getPageSize();
        long pageNum = pictureQueryRequest.getPageNum();
        Page<Picture> page = pictureService.page(new Page<>(pageNum, pageSize), pictureQueryWrapper);
        return ResultUtils.success(page);
    }
    /**
     * 分页获取图片列表（用户用，需要脱敏）
     */
    @GetMapping("/list/vo")
    public BaseResponse<Page<PictureVo>> listUserPicture(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //判断查询公共图库还是用户空间的图片
        Long spaceId = pictureQueryRequest.getSpaceId();
        if( spaceId == null){
            //获取公共图库的图片
            pictureQueryRequest.setIsSpaceNull(true);
            //用户只能获取审核通过的图片列表，设置查询请求中审核状态为通过
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getCode());
        }else{
            //获取用户空间的图片
            //User loginUser = userService.getLoginUser(request);
            //Long userId = spaceService.getById(spaceId).getUserId();
            //ThrowUtils.throwIf(!userId.equals(loginUser.getId()), ErrorCode.NOT_FOUND_ERROR, "无权限");
            //使用Sa-Token编程式身份验证
            boolean b = StpKit.SPACE.hasPermission(spaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!b,ErrorCode.NO_AUTH_ERROR, "没有权限");

        }
        //获取图片列表
        QueryWrapper<Picture> pictureQueryWrapper = pictureService.queryPageWrapper(pictureQueryRequest);
        long pageSize = pictureQueryRequest.getPageSize();
        long pageNum = pictureQueryRequest.getPageNum();
        //限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.NOT_FOUND_ERROR);
        Page<Picture> page = pictureService.page(new Page<>(pageNum, pageSize), pictureQueryWrapper);
        //获取脱敏后的图片列表
        Page<PictureVo> vopage = pictureService.getPictureVoPage(page, request);
        return ResultUtils.success(vopage);
    }

    /**
     * 更新图片(管理员)
     */
    @PostMapping("/update")
    @AuthCheck(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //判断图片是否存在
        Picture oldPicture = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //类型转换
        Picture picture = BeanUtil.copyProperties(pictureUpdateRequest, Picture.class);
        String jsonStr = JSONUtil.toJsonStr(pictureUpdateRequest.getTags());
        picture.setTags(jsonStr);
        //身份校验
        //pictureService.checkPictureSpace(oldPicture, userService.getLoginUser(request));
        //设置入库图片的空间id
        picture.setSpaceId(oldPicture.getSpaceId());
        //设置图片审核状态
        User loginUser = userService.getLoginUser(request);
        pictureService.setPictureReviewStatus(picture,loginUser);
        //设置更新时间
        picture.setUpdateTime(new Date());
        //数据校验
        pictureService.checkPicture(picture);
        //修改数据库
        boolean b = pictureService.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "修改失败");
        return ResultUtils.success(true);
    }

    /**
     * 编辑图片（用户）
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //判断图片是否存在
        ThrowUtils.throwIf(pictureService.getById(pictureEditRequest.getId()) == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //类型转换
        Picture picture = BeanUtil.copyProperties(pictureEditRequest, Picture.class);
        String jsonStr = JSONUtil.toJsonStr(pictureEditRequest.getTags());
        picture.setTags(jsonStr);
        //设置编辑时间
        picture.setEditTime(new Date());
        //设置入库图片的空间id
        picture.setSpaceId(pictureService.getById(pictureEditRequest.getId()).getSpaceId());
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //数据校验
        pictureService.checkPictureSpace(picture, loginUser);
        //设置图片审核状态
        pictureService.setPictureReviewStatus(picture,loginUser);
        //修改数据库
        boolean b = pictureService.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "修改失败");
        return ResultUtils.success(true);
    }

    /**
     * 获取置顶标签和分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagsList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 管理员审核图片
     */
    @PostMapping("/review")
    @AuthCheck(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        pictureService.reviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }
    /**
     * 批量抓取和创建图片
     */
    @PostMapping("upload/batch")
    @AuthCheck(value = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUpLoadByBatchRequest pictureUpLoadByBatchRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureUpLoadByBatchRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        Integer loadCount = pictureService.uploadPictureByBatch(pictureUpLoadByBatchRequest, loginUser);
        return ResultUtils.success(loadCount);
    }

    /**
     * 以图搜图
     */
    @PostMapping("/search/byPicture")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<imageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //判断图片是否存在
        Picture oldPicture = pictureService.getById(searchPictureByPictureRequest.getPictureId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //调用Api
        List<imageSearchResult> imageSearchResults = imageSearchApiFacade.searchImage(oldPicture.getUrl());
        if(CollUtil.isEmpty(imageSearchResults)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到相似图片");
        }
        return ResultUtils.success(imageSearchResults);
    }

    /**
     * 根据图片主色调查询用户空间内的图片
     */

    @PostMapping("/search/byColor")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVo>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //判断用户空间是否存在
        long spaceId = searchPictureByColorRequest.getSpaceId();
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "用户空间不存在");
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        String picColor = searchPictureByColorRequest.getPicColor();
        List<PictureVo> similarPictureList = pictureService.getSimilarPictureList(spaceId, picColor, loginUser);
        return ResultUtils.success(similarPictureList);
    }

    /**
     * 批量管理照片
     */
    @PostMapping("/manage/batch")
    @AuthCheck(value = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> managePictureBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        pictureService.updateBatchPicture(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }


    /**
     * 创建Ai扩图任务
     */
    @PostMapping("/expand")
    @SaSpaceCheckPermission(value = spaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> expandPicture(@RequestBody CreatPictureOutPaintingRequest creatPictureOutPaintingRequest, HttpServletRequest request) {
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        CreateOutPaintingTaskResponse createOutPaintingTaskResponse = pictureService.PictureOutPainting(creatPictureOutPaintingRequest, loginUser);
        return ResultUtils.success(createOutPaintingTaskResponse);
    }
    /**
     * 查询ai扩图任务
     */
    @GetMapping("/expand/taskId")
    public BaseResponse<GetOutPaintingTaskResponse> getOutPaintingTask( String taskId) {
        //判空
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.NOT_FOUND_ERROR, "任务id不能为空");
        AliyunAiApi aliyunAiApi = new AliyunAiApi();
        GetOutPaintingTaskResponse getOutPaintingTaskResponse = aliyunAiApi.GetOutPaintingTask(taskId);
        return ResultUtils.success(getOutPaintingTaskResponse);
    }

    /**
     * 多级缓存获取图片分页(Caffeine+Redis)
     */
    @Deprecated
    @PostMapping("list/page/vo/cache")
    public BaseResponse<Page<PictureVo>> listPictureVOByPageFromCache(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest request) {
        //判断请求是否为空
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.NOT_FOUND_ERROR);
        //获取页码和页大小
        int pageNum = pictureQueryRequest.getPageNum();
        int pageSize = pictureQueryRequest.getPageSize();
        //限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.NOT_FOUND_ERROR, "爬虫行为");
        //用户只能获取已经审核过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getCode());
        //构造查询key
        String jsonStr = JSONUtil.toJsonStr(pictureQueryRequest);
        byte[] bytes = jsonStr.getBytes();
        String hashKey = DigestUtils.md5DigestAsHex(bytes);
        //构建多级key
        String key = StrUtil.format("yunPhoto:pageVo:{}", hashKey);
        //从caffeine缓存中获取图片分页
        //构建本地缓存
        Cache<String, String> LOCAL_CACHE =
                Caffeine.newBuilder().initialCapacity(1024)
                        .maximumSize(10000L)
                        // 缓存 5 分钟移除
                        .expireAfterWrite(5L, TimeUnit.MINUTES)
                        .build();
        String string1 = LOCAL_CACHE.getIfPresent(key);
        if (StrUtil.isNotBlank(string1)) {
             Page<PictureVo> bean = JSONUtil.toBean(string1,Page.class);
             return ResultUtils.success(bean);
        }
        //本地缓存没有，从redis缓存中获取图片分页
        //设置null值，防止缓存穿透
        LOCAL_CACHE.put(key,null);
        String string = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(string)) {
             Page<PictureVo> bean = JSONUtil.toBean(string,Page.class);
             //将数据存入本地缓存
             LOCAL_CACHE.put(key,string);
             return ResultUtils.success(bean);
        }
        //使用分布式锁,防止缓存击穿
        RLock lock = redissonClient.getLock("lock");
        Page<PictureVo> pictureVoPage = null;
        try {
            lock.tryLock(10, 10, TimeUnit.SECONDS);
            if(lock.isLocked()) {
            //如果两级缓存中没有，则从数据库中获取
            QueryWrapper<Picture> pictureQueryWrapper = pictureService.queryPageWrapper(pictureQueryRequest);
            Page<Picture> picturePage = pictureService.page(new Page<>(pageNum, pageSize), pictureQueryWrapper);
            //将图片分页封装成图片vo分页
            pictureVoPage = pictureService.getPictureVoPage(picturePage, request);
            //将图片vo分页存入redis缓存
            //设置缓存过期时间
            long l = RandomUtil.randomLong(1, 24);
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(pictureVoPage), l, TimeUnit.SECONDS);
            //将图片vo分页存入本地缓存
            LOCAL_CACHE.put(key, JSONUtil.toJsonStr(pictureVoPage));
            //返回图片
            //return ResultUtils.success(pictureVoPage);
            }else {
                return null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //判断是否持有锁
            if(lock != null && lock.isLocked()){
                //是否是当前线程持有锁
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }
        //返回图片
        return ResultUtils.success(pictureVoPage);
    }


}
