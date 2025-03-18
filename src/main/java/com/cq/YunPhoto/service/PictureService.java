package com.cq.YunPhoto.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cq.YunPhoto.Api.Aliyun.model.CreateOutPaintingTaskResponse;
import com.cq.YunPhoto.Api.Aliyun.model.GetOutPaintingTaskResponse;
import com.cq.YunPhoto.Model.dto.Picture.*;
import com.cq.YunPhoto.Model.dto.user.UserQueryRequest;
import com.cq.YunPhoto.Model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86198
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-02-27 16:47:56
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param userInfo
     * @param inputSource
     * @param pictureUploadRequest
     * @return
     */
    PictureVo uploadPicture(User userInfo, Object inputSource, PictureUploadRequest pictureUploadRequest);

    /**
     * 获取脱敏后的图片
     * @param picture
     * @return
     */
    PictureVo getPictureVo(Picture picture);

    /**
     * 分页查询图片
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> queryPageWrapper(PictureQueryRequest pictureQueryRequest);


    //此方法自己写的

    /**
     * 分页获取图片封装（用户用）
     * @param page
     * @param request
     * @return
     */
    Page<PictureVo> getPictureVoPage(Page<Picture> page, HttpServletRequest request);

    /**
     * 图片数据校验
     */
    void checkPicture(Picture picture);

    /**
     * 图片审核
     */
    void reviewPicture(PictureReviewRequest pictureReviewRequest,User loginUser);

    /**
     * 设置图片审核状态
     */
    void setPictureReviewStatus(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     */
    Integer uploadPictureByBatch(PictureUpLoadByBatchRequest pictureUpLoadByBatchRequest, User loginUser);

    /**
     * 删除cos中的图片
     */
    void deletePictureFromCos(Picture picture);

    /**
     * 图片权限校验（spaceId）
     */
    void checkPictureSpace(Picture picture, User loginUser);

    /**
     * 根据图片主色调获取相似图片列表（用户空间内查询）
     */
    List<PictureVo> getSimilarPictureList(long spaceId, String color, User loginUser);


    /**
     * 批量修改图片
     */
    void updateBatchPicture(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * Ai扩图
     */
    CreateOutPaintingTaskResponse PictureOutPainting(CreatPictureOutPaintingRequest creatPictureOutPaintingRequest, User loginUser);
}
