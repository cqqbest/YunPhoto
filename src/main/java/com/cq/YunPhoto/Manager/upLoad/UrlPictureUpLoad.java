package com.cq.YunPhoto.Manager.upLoad;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.cq.YunPhoto.Config.CosClientConfig;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.dto.File.UpLoadPictureResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpLoad extends PictureUpLoadTemplate{
    @Override
    public UpLoadPictureResult bulidResult(File tempFile, ImageInfo imageInfo, CosClientConfig cosClientConfig, Object inputSource, String loadFilePath) {
        UpLoadPictureResult upLoadPictureResult = new UpLoadPictureResult();
        upLoadPictureResult.setUrl(cosClientConfig.getHost() + "/" +loadFilePath);
        upLoadPictureResult.setPicWidth(imageInfo.getWidth());
        upLoadPictureResult.setPicHeight(imageInfo.getHeight());
        upLoadPictureResult.setPicSize(FileUtil.size(tempFile));
        upLoadPictureResult.setName(FileUtil.mainName((String)inputSource));
        upLoadPictureResult.setPicFormat(imageInfo.getFormat());
        upLoadPictureResult.setPicScale(NumberUtil.toDouble(imageInfo.getWidth() * 2.0 / imageInfo.getHeight()));
        //设置图片主色调
        upLoadPictureResult.setPicColor(imageInfo.getAve());
        return upLoadPictureResult;
    }

    @Override
    public void dataCheck(Object inputSource) {
        String url = (String) inputSource;
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

    @Override
    public String getFfix(Object inputSource) {
        String url = (String)inputSource;
        return FileUtil.getSuffix(url);
    }

    @Override
    public void cateTempFile(Object inputSource, File tempFile) throws Exception {
        String url = (String)inputSource;
        HttpUtil.downloadFile(url, tempFile);
    }
}
