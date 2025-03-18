package com.cq.YunPhoto.Manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class spaceUserRole implements Serializable {

        private String key;

        private String name;

        /**
         * 用户权限列表
         */
        private List<String> permission;

        private String description;

        private static final long serialVersionUID = 1L;
}
