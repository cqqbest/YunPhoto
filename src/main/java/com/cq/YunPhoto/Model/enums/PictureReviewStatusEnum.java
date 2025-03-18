package com.cq.YunPhoto.Model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Data;
import lombok.Getter;

@Getter
public enum PictureReviewStatusEnum {
    REVIEWING(0, "待审核"),
    PASS(1, "通过"),
    REJECT(2, "拒绝");
    private final int code;
    private final String message;
    PictureReviewStatusEnum(int  code, String message) {
        this.code = code;
        this.message = message;
    }
    //根据code获取message
    public static String getMessageByCode(Integer code) {
        if(ObjUtil.isEmpty(code)) {
            return null;
        }
        for(PictureReviewStatusEnum statusEnum : PictureReviewStatusEnum.values()) {
            if(statusEnum.getCode() == code) {
                return statusEnum.getMessage();
            }
        }
        return null;
    }
}
