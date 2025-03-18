package com.cq.YunPhoto.Manager.WebSocket.Model;

import lombok.Getter;

@Getter
public enum PictureEditActionEnum {
    ZOOM_IN("放大操作","ZOOM_IN"),
    ZOOM_OUT("缩小操作","ZOOM_OUT"),
    LEFT_ROTATE("左旋转操作","LEFT_ROTATE"),
    RIGHT_ROTATE("右旋转操作","RIGHT_ROTATE");

    private final String text;

    private final String value;

    PictureEditActionEnum(String text, String value){
        this.text = text;
        this.value = value;
    }

    public static PictureEditActionEnum getEnumByValue(String value){
        for(PictureEditActionEnum actionEnum : PictureEditActionEnum.values()){
            if(actionEnum.getValue().equals(value)){
                return actionEnum;
            }
        }
        return null;
    }

}
