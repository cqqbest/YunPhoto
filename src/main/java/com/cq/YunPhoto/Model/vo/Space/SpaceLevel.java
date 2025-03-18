package com.cq.YunPhoto.Model.vo.Space;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor//全参构造器
public class SpaceLevel {
    private String text;
    private int  value;
    private Long maxCount;
    private Long maxSize;
}
