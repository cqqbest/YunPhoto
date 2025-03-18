package com.cq.YunPhoto.Common;

import lombok.Data;

/**
 * 分页请求封装
 */

@Data
public class PageRequest {
    //当前页码
    private int pageNum  = 1;
    //每页大小
    private int pageSize = 10;
    //排序字段
    private String sort;
    //排序规则(默认降序)
    private String order = "descend";
}
