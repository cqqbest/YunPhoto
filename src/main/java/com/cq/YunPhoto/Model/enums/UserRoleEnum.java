package com.cq.YunPhoto.Model.enums;


import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举
 */

@Getter
public enum UserRoleEnum {
    USER("user", "普通用户"),
    ADMIN("admin", "管理员");

    private final String code;
    private final String desc;

    UserRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     * @param code
     * @return
     */
    public static UserRoleEnum getByCode(String code) {
        if(ObjUtil.isEmpty(code)){
            return null;
        }
        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            if (roleEnum.getCode().equals(code)) {
                return roleEnum;
            }
        }
        return null;
    }
}
