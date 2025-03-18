package com.cq.YunPhoto.Manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.ResultUtils;
import com.cq.YunPhoto.Config.CosClientConfig;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.dto.File.UpLoadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片（本地）
     * @param multipartFile
     * @param upLoadPathPrefix
     * @return
     */
    public UpLoadPictureResult uploadPicture(MultipartFile multipartFile,String upLoadPathPrefix) {
        //文件校验
        dataCheck(multipartFile);
        //编写文件路径
        String uuid = RandomUtil.randomString(16);
        String loadFilePath = StrUtil.format("%s_%s.%s", upLoadPathPrefix, uuid, FileUtil.getSuffix(multipartFile.getOriginalFilename()));
        File tempFile = null;
        //上传文件
        try {
            //生成临时文件
            tempFile = File.createTempFile(loadFilePath,null);
            //将文件写入临时文件
            multipartFile.transferTo(tempFile);
            //上传文件
            PutObjectResult putObjectResult = cosManager.uploadFileAndParse(tempFile, loadFilePath);
            //获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //构造返回结果
            UpLoadPictureResult upLoadPictureResult = new UpLoadPictureResult();
            upLoadPictureResult.setUrl(cosClientConfig.getHost() + loadFilePath);
            upLoadPictureResult.setPicWidth(imageInfo.getWidth());
            upLoadPictureResult.setPicHeight(imageInfo.getHeight());
            upLoadPictureResult.setPicSize(FileUtil.size(tempFile));
            upLoadPictureResult.setName(multipartFile.getOriginalFilename());
            upLoadPictureResult.setPicFormat(imageInfo.getFormat());
            upLoadPictureResult.setPicScale(NumberUtil.toDouble(imageInfo.getWidth() * 2.0 / imageInfo.getHeight()));
            return upLoadPictureResult;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        }finally {
            //删除临时文件
            deleteTempFile(tempFile);
        }

    }


    /**
     * 上传图片（URL）
     */
    public UpLoadPictureResult uploadPicture(String url,String upLoadPathPrefix) {
        //url检验
        dataCheck(url);
        //编写文件路径
        String uuid = RandomUtil.randomString(16);
        String loadFilePath = StrUtil.format("%s_%s.%s", upLoadPathPrefix, uuid, FileUtil.getSuffix(url));
        //上传文件
        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = File.createTempFile(loadFilePath, null);
            // multipartFile.transferTo(file);
            HttpUtil.downloadFile(url, tempFile);
            //上传文件
            PutObjectResult putObjectResult = cosManager.uploadFileAndParse(tempFile, loadFilePath);
            //获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //构造返回结果
            UpLoadPictureResult upLoadPictureResult = new UpLoadPictureResult();
            upLoadPictureResult.setUrl(cosClientConfig.getHost() + loadFilePath);
            upLoadPictureResult.setPicWidth(imageInfo.getWidth());
            upLoadPictureResult.setPicHeight(imageInfo.getHeight());
            upLoadPictureResult.setPicSize(FileUtil.size(tempFile));
            upLoadPictureResult.setName(tempFile.getName());
            upLoadPictureResult.setPicFormat(imageInfo.getFormat());
            upLoadPictureResult.setPicScale(NumberUtil.toDouble(imageInfo.getWidth() * 2.0 / imageInfo.getHeight()));
            return upLoadPictureResult;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        }finally {
            //删除临时文件
            deleteTempFile(tempFile);
        }
    }

    /**
     * 数据校验（文件）
     */
    private void dataCheck(MultipartFile multipartFile){
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"文件不能为空");
        ThrowUtils.throwIf(multipartFile.getSize() > 1024 * 1024 * 2, ErrorCode.PARAMS_ERROR,"文件大小不能超过2M");
        List<String> list = Arrays.asList("jepg", "png", "webp", "jpg");
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!list.contains(suffix), ErrorCode.PARAMS_ERROR,"文件格式不正确");
    }


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

    /**
     * 数据校验（url）
     */
    public void dataCheck(String url){
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {
            // 1. 验证 URL 格式  
            new URL(url); // 验证是否是合法的 URL  
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 校验 URL 协议  
        ThrowUtils.throwIf(!(url.startsWith("http://") || url.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        // 3. 发送 HEAD 请求以验证文件是否存在  
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, url).execute();
            // 未正常返回，无需执行其他判断  
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件类型  
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型  
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小  
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB  
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    
    
}
