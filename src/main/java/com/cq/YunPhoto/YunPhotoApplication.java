package com.cq.YunPhoto;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync//开启异步
@MapperScan("com.cq.YunPhoto.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)//代理
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})//排除sharding配置

public class YunPhotoApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunPhotoApplication.class, args);
    }

}
