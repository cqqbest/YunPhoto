package com.cq.YunPhoto.Model.enums;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户空间角色枚举：viewer/editor/admin
 */
@Getter
public enum SpaceUserRoleEnum {
    VIEWER("viewer","浏览者"),
    EDITOR("editor","编辑者"),
    ADMIN("admin","管理员");

    private final String value;
    private final String text;
    SpaceUserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据value获取枚举
     */
    public static SpaceUserRoleEnum getEnumByValue(String value) {
        for (SpaceUserRoleEnum spaceUserRoleEnum : SpaceUserRoleEnum.values()) {
            if (spaceUserRoleEnum.getValue().equals(value)) {
                return spaceUserRoleEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有value
     */
    public static List<String> getAllValues() {
        return Arrays.
                stream(SpaceUserRoleEnum.values())
                .map(SpaceUserRoleEnum::getValue)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有text
     */
    public static List<String> getAllTexts() {
        return Arrays
                .stream(SpaceUserRoleEnum.values())
                .map(SpaceUserRoleEnum::getText)
                .collect(Collectors.toList());
    }

}
