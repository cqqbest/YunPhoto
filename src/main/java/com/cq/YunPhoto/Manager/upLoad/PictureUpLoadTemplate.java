package com.cq.YunPhoto.Manager.upLoad;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.cq.YunPhoto.Config.CosClientConfig;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Manager.CosManager;
import com.cq.YunPhoto.Model.dto.File.UpLoadPictureResult;
import com.cq.YunPhoto.service.impl.PictureServiceImpl;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@Slf4j
@Component
public abstract class PictureUpLoadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public final UpLoadPictureResult uploadPicture(Object inputSource, String upLoadPathPrefix){
        //1.数据校验
        dataCheck(inputSource);
        //2.生成文件名
        String uuid = RandomUtil.randomString(16);
        String loadFilePath = StrUtil.format("%s_%s.%s", upLoadPathPrefix, uuid, getFfix(inputSource));
        //3.上传文件
        File TempFile = null;
        try {
            //创建临时文件
            TempFile = File.createTempFile(loadFilePath, null);
            //将文件写入临时文件
            cateTempFile(inputSource,TempFile);
            //上传文件
            PutObjectResult putObjectResult = cosManager.uploadFileAndParse(TempFile, loadFilePath);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            //拿到图片主色调
            String ave = imageInfo.getAve();
            if(CollUtil.isNotEmpty(objectList)){
                //获取压缩图信息
                CIObject ciObject = objectList.get(0);
                //默认缩略图为压缩图
                CIObject ciObject1 = ciObject;
                if(objectList.size() > 1){
                    //获取缩略图信息
                    ciObject1 = objectList.get(1);
                }
                //返回缩略图封装结果
                return bulidWebpResult(inputSource,ciObject,ciObject1,imageInfo);
            }
            //4.返回结果
            return bulidResult(TempFile, imageInfo,cosClientConfig,inputSource,loadFilePath);
        } catch (Exception e) {
            log.error("上传失败",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        }finally {
            deleteTempFile(TempFile);
        }
    }

    public abstract UpLoadPictureResult bulidResult(File tempFile, ImageInfo imageInfo, CosClientConfig cosClientConfig, Object inputSource,String loadFilePath);

    public abstract void dataCheck(Object inputSource);
    public abstract String getFfix(Object inputSource);
    public abstract void cateTempFile(Object inputSource,File TempFile) throws Exception;

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file){
        if(file == null){
            return;
        }
        boolean del = FileUtil.del(file);
        if(!del){
            log.error("删除临时文件失败");
        }
    }

    public UpLoadPictureResult bulidWebpResult(Object inputSource,CIObject ciObject, CIObject ciObject1, ImageInfo imageInfo){
        String picName = null;
        if(inputSource instanceof MultipartFile){
            picName = ((MultipartFile) inputSource).getOriginalFilename();
        }else {
            picName = FileUtil.mainName((String) inputSource);
        }
        UpLoadPictureResult upLoadPictureResult = new UpLoadPictureResult();
        upLoadPictureResult.setName(picName);
        upLoadPictureResult.setPicFormat(ciObject.getFormat());
        upLoadPictureResult.setPicHeight(ciObject.getHeight());
        upLoadPictureResult.setPicWidth(ciObject.getWidth());
        upLoadPictureResult.setPicSize(Long.valueOf(ciObject.getSize()));
        double picScale = NumberUtil.round(ciObject.getWidth() * 1.0 / ciObject.getHeight(), 2).doubleValue();
        upLoadPictureResult.setPicScale(picScale);
        upLoadPictureResult.setUrl(cosClientConfig.getHost()+"/"+ciObject.getKey());
        upLoadPictureResult.setThumbnailUrl(cosClientConfig.getHost()+"/"+ciObject1.getKey());
        //设置图片主色调
        upLoadPictureResult.setPicColor(imageInfo.getAve());
        return upLoadPictureResult;
    }




}
