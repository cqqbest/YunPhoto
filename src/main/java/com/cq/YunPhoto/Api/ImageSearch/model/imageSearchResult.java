package com.cq.YunPhoto.Api.ImageSearch.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class imageSearchResult {
    /**
     * 缩略图url
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;
}
