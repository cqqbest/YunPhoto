package com.cq.YunPhoto.Model.enums;

import lombok.Data;
import lombok.Getter;

/**
 * 空间级别枚举
 */
@Getter
public enum SpaceLevelEnum {

    COMMON("普通空间",0,100L,100L*1024*1024),
    PROFESSIONAL("专业空间",1,1000L,1000L*1024*1024*1024),
    FLAGSHIP("旗舰空间",2,10000L,10000L*1024*1024*1024*1024);

    private final String text;
    private final Integer value;
    private final Long maxCount;
    private final Long maxSize;


    SpaceLevelEnum(String text,Integer value,Long maxCount,Long maxSize){
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }


    //根据value获取枚举
    public static SpaceLevelEnum getSpaceLevelEnum(Integer value){

        //校验
        if (value == null){
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()){
            if (spaceLevelEnum.getValue().equals(value)){
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
