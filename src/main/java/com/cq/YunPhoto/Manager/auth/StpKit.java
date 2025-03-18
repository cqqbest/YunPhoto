package com.cq.YunPhoto.Manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class StpKit {

    public static final String SPACE_TYPE = "space";

    /**
     * 默认原生会话对象，本项目中未使用
     */
    public static final StpLogic DEFAULT = StpUtil.getStpLogic();

    /**
     * 空间会话对象
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);

}
