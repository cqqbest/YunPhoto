package com.cq.YunPhoto.Manager.upLoad;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.cq.YunPhoto.Config.CosClientConfig;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.dto.File.UpLoadPictureResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class FilePictureUpLoad extends PictureUpLoadTemplate{

    @Override
    public UpLoadPictureResult bulidResult(File tempFile, ImageInfo imageInfo, CosClientConfig cosClientConfig, Object inputSource, String loadFilePath) {
        UpLoadPictureResult upLoadPictureResult = new UpLoadPictureResult();
        upLoadPictureResult.setUrl(cosClientConfig.getHost() +"/"+ loadFilePath);
        upLoadPictureResult.setPicWidth(imageInfo.getWidth());
        upLoadPictureResult.setPicHeight(imageInfo.getHeight());
        upLoadPictureResult.setPicSize(FileUtil.size(tempFile));
        upLoadPictureResult.setName(((MultipartFile)inputSource).getOriginalFilename());
        upLoadPictureResult.setPicFormat(imageInfo.getFormat());
        upLoadPictureResult.setPicScale(NumberUtil.toDouble(imageInfo.getWidth() * 2.0 / imageInfo.getHeight()));
        //设置图片主色调
        upLoadPictureResult.setPicColor(imageInfo.getAve());
        return upLoadPictureResult;
    }

    @Override
    public void dataCheck(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"文件不能为空");
        ThrowUtils.throwIf(multipartFile.getSize() > 1024 * 1024 * 2, ErrorCode.PARAMS_ERROR,"文件大小不能超过2M");
        List<String> list = Arrays.asList("jepg", "png", "webp", "jpg");
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!list.contains(suffix), ErrorCode.PARAMS_ERROR,"文件格式不正确");

    }

    @Override
    public String getFfix(Object inputSource) {
        return FileUtil.getSuffix(((MultipartFile)inputSource).getOriginalFilename());
    }

    @Override
    public void cateTempFile(Object inputSource, File tempFile) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(tempFile);
    }
}
