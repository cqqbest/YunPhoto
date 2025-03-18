package com.cq.YunPhoto.Api.Aliyun.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;

/**
 * 查询响应类
 */
@Data
public class GetOutPaintingTaskResponse implements Serializable {
    /**
     * 输出的任务信息
     */
    private OutPut output;


    /**
     * 图像统计信息
     */
    private Usage usage;

    public static class OutPut implements Serializable {
        /**
         * 任务id
         */
        private String taskId;

        /**
         * 任务状态
         */
        private String taskStatus;

        /**
         * 任务结果统计
         */

        private TaskMetrics taskMetrics;


        /**
         * 任务提交时间
         */
        private String submitTime;
        /**
         * 任务完成时间
         */
        private String endTime;
        /**
         * 输出图像URL地址
         */
        private String outputImageUrl;
        /**
         * 请求失败的错误码
         */
        private String code;
        /**
         * 请求失败的错误信息
         */
        private String message;
    }

    public static class TaskMetrics implements Serializable {
        /**
         * 总的任务数
         */
        private int TOTAL;

        /**
         * 成功的任务数
         */
        private int SUCCEEDED;

        /**
         * 失败的任务数
         */
        private int FAILED;
    }

    public static class Usage implements Serializable {
        /**
         * 模型生成图片数量
         */
        private int imageCount;
    }

    /**
     * 请求唯一标识
     */
    private String requestId;




    private static final long serialVersionUID = 1L;


}
