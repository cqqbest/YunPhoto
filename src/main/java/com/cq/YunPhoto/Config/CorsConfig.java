package com.cq.YunPhoto.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置，因为前后端端口号不一样，所以会产生跨域问题
 */

@Configuration
public class CorsConfig  implements WebMvcConfigurer {
    //跨域配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //允许的跨域请求如下
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedHeaders("*");
    }
}
