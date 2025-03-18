package com.cq.YunPhoto.Api.Aliyun.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;

/**
 * 创建响应类
 */
@Data
public class CreateOutPaintingTaskResponse implements Serializable {

    /**
     * 任务输出信息
     */
    private Output output;

    @Data
    public static class Output implements Serializable {
        /**
         * 任务id
         */
        private String taskId;
        /**
         * 任务状态
         */
        private String taskStatus;
    }

    /**
     * 请求唯一标识
     */
    private String requestId;
    /**
     * 请求失败错误码
     */
    private String code;
    /**
     * 请求失败错误信息
     */
    private String message;

    private static final long serialVersionUID = 1L;
}
