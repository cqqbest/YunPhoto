package com.cq.YunPhoto.Manager.WebSocket.Model;

import lombok.Data;
import lombok.Getter;

@Getter
public enum PictureEditMessageType {
    INFO("发送通知", "INFO"),
    ERROR("发送错误", "ERROR"),
    ENTER_EDIT("进入编辑状态", "ENTER_EDIT"),
    EXIT_EDIT("退出编辑状态", "EXIT_EDIT"),
    EDIT_ACTION("执行编辑操作", "EDIT_ACTION");

    private String text;

    private String value;


    PictureEditMessageType(String text, String value) {
        this.text = text;
        this.value = value;
    }


    /**
     * 根据value获取枚举类型
     * @param value
     * @return
     */
    public static PictureEditMessageType getEnumByValue(String value) {
        for (PictureEditMessageType type : PictureEditMessageType.values()) {
            if(type.getValue().equals(value)){
                return type;
            }
        }
        return null;
    }
}
