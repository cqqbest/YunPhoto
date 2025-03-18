package com.cq.YunPhoto.Controller;

import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康测试
 */
@RestController
public class HealthController {
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtils.success("success");
    }
}
