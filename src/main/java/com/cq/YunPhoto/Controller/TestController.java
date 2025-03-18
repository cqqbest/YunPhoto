package com.cq.YunPhoto.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * knife4j测试
 */

@Controller//换ResController直接向前端返回字符
public class TestController {
    @GetMapping("/hello")
    @ResponseBody//因为Controller注解只能向前端返回视图，所以加这个注释
    public String hellotest(){
        return "hello";
    }

}
