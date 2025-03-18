package com.cq.YunPhoto.Model.vo.Space;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.entity.Space;
import com.cq.YunPhoto.Model.vo.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SpaceVo implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

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

    private static final long serialVersionUID = 1L;

    /**
     * 空间类型：私有/团队
     */
    private Integer spaceType;


    /**
     * 获取权限列表
     */
    private List<String> permissionList;


    /**
     * space转spaceVo
     */
    public static SpaceVo toSpaceVo(Space space){
        //判空
        ThrowUtils.throwIf(ObjUtil.isEmpty(space), ErrorCode.NOT_FOUND_ERROR,"space不能为空");
        //转换
        SpaceVo spaceVo = new SpaceVo();
        BeanUtil.copyProperties(space,spaceVo);
        return spaceVo;
    }

    /**
     * spaceVo转space
     */
    public static Space toSpace(SpaceVo spaceVo){
        //判空
        ThrowUtils.throwIf(ObjUtil.isEmpty(spaceVo), ErrorCode.NOT_FOUND_ERROR,"spaceVo不能为空");
        //转换
        Space space = new Space();
        BeanUtil.copyProperties(spaceVo,space);
        return space;
    }
}
