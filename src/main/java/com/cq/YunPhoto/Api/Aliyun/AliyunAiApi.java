package com.cq.YunPhoto.Api.Aliyun;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.cq.YunPhoto.Api.Aliyun.model.CreateOutPaintingTaskRequest;
import com.cq.YunPhoto.Api.Aliyun.model.CreateOutPaintingTaskResponse;
import com.cq.YunPhoto.Api.Aliyun.model.GetOutPaintingTaskResponse;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "aliyun")
public class AliyunAiApi {

    @Value("${aliyun.apiKey")
    private String ApiKey;


    public static final String CREATE_OUT_PAINTING_TASK = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-paintin";
    public static final String GET_OUT_PAINTING_TASK = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    public   CreateOutPaintingTaskResponse CreateOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        //判断请求是否为空
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        }
        //构建请求
        HttpRequest body = HttpRequest.post(CREATE_OUT_PAINTING_TASK)
                .header("Content-Type", "json")
                .header("Authorization", "Bearer " + ApiKey)
                .header("X-DashScope-Async", "enable")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        //判断请求是否成功
        try(HttpResponse execute = body.execute()){
            if (execute.getStatus() != 200) {
                log.error("请求失败{}",execute.body());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"请求失败");
            }
            //返回结果
            CreateOutPaintingTaskResponse response = JSONUtil.toBean(execute.body(), CreateOutPaintingTaskResponse.class);
            String code = response.getCode();
            if(StrUtil.isNotBlank(code)){
                log.error("任务请求失败，错误码{}，错误信息{}",code,response.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"请求失败");
            }
        return response;
        }
    }

    public GetOutPaintingTaskResponse GetOutPaintingTask(String taskId) {
        //判断请求是否为空
        if (taskId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id为空");
        }
        String format = StrUtil.format(GET_OUT_PAINTING_TASK, taskId);
        HttpRequest authorization = HttpRequest.get(format)
                .header("Authorization", "Bearer " + ApiKey);
        //判断请求是否成功
        try (HttpResponse execute = authorization.execute()) {
            if (execute.getStatus() != 200) {
                log.error("请求失败{}",execute.body());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求失败");
            }
            //返回结果
            return JSONUtil.toBean(execute.body(), GetOutPaintingTaskResponse.class);
        }
    }


}
