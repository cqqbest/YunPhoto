package com.cq.YunPhoto.Model.vo;


import cn.hutool.core.bean.BeanUtil;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户
     */
    private UserVO user;

    /**
     * 缩略图url
     */
    private String thumbnailUrl;

    /**
     * 用户空间id
     */
    private Long spaceId;

    /**
     * 获取权限列表
     */
    private List<String> permissionList;

    private static final long serialVersionUID = 1L;

    //封装类转对象
    public  static Picture toPicture(PictureVo pictureVo){
        ThrowUtils.throwIf(pictureVo == null, ErrorCode.NOT_FOUND_ERROR);
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVo, picture);
        return picture;
    }
    //对象转封装类
    public static PictureVo toPictureVo(Picture picture){
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        PictureVo pictureVo = new PictureVo();
        BeanUtil.copyProperties(picture, pictureVo);
        return pictureVo;
    }
}
