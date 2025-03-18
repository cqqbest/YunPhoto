package com.cq.YunPhoto.Model.enums;

import lombok.Getter;

@Getter
public enum SpaceTypeEnum {
    PRIVATE(0,"私有空间"),
    TEAM(1,"团队空间");
    private final Integer code;
    private final String desc;
    SpaceTypeEnum(Integer code,String desc){
        this.code = code;
        this.desc = desc;
    }
    //根据code获取枚举
    public static SpaceTypeEnum getSpaceTypeEnumByCode(Integer code){
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.getCode().equals(code)){
                return spaceTypeEnum;
            }
        }
        return null;
    }

    //根据desc获取枚举
    public static SpaceTypeEnum getSpaceTypeEnumByDesc(String desc){
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.getDesc().equals(desc)){
                return spaceTypeEnum;
            }
        }
        return null;
    }

}
