package com.cq.YunPhoto.Model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.cq.YunPhoto.Model.entity.SpaceUser;
import com.cq.YunPhoto.Model.vo.Space.SpaceVo;
import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUserVo implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 用户视图
     */
    private UserVO userVo;

    /**
     * 空间视图
     */
    private SpaceVo spaceVo;


    private static final long serialVersionUID = 1L;


    /**
     * spaceUser转spaceUserVo
     */
    public static SpaceUserVo SpaceUserToVo(SpaceUser spaceUser){
        SpaceUserVo spaceUserVo = new SpaceUserVo();
        if(spaceUser != null){
            BeanUtil.copyProperties(spaceUser,spaceUserVo);
        }
        return spaceUserVo;
    }
    /**
    *spaceUserVo转spaceUser
     */
    public static SpaceUser VoToSpaceUserVo(SpaceUserVo spaceUserVo){
        SpaceUser spaceUser = new SpaceUser();
        if(spaceUserVo != null){
            BeanUtil.copyProperties(spaceUserVo,spaceUser);
        }
        return spaceUser;
    }
}
