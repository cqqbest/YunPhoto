package com.cq.YunPhoto.Model.dto.Picture;

import com.cq.YunPhoto.Api.Aliyun.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;
@Data
public class CreatPictureOutPaintingRequest implements Serializable {
    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 请求参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;
}
