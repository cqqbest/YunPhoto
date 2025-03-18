package com.cq.YunPhoto.Api.Aliyun.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Ai扩图请求类
 */
@Data
public class CreateOutPaintingTaskRequest implements Serializable {
    private String model = "image-out-painting";
    private Input input;
    private Parameters parameters;

    @Data
    public static class Input{
        @Alias("image_url")
        private String imageUrl;
    }
    @Data
    public static class Parameters{

        private int angle;
        @Alias("output_ratio")
        private String outputRatio;
        @Alias("x_scale")
        @JsonProperty("xScale")
        private float xScale;
        @Alias("y_scale")
        @JsonProperty("yScale")
        private float yScale;
        @Alias("top_offset")
        private int topOffset;
        @Alias("bottom_offset")
        private int bottomOffset;
        @Alias("left_offset")
        private int leftOffset;
        @Alias("right_offset")
        private int rightOffset;
        @Alias("best_quality")
        private boolean bestQuality;
        @Alias("limit_image_size")
        private boolean limitImageSize;
        @Alias("add_watermark")
        private boolean addWatermark = true;
    }
    private static final long serialVersionUID = 1L;
}
