package com.cq.YunPhoto.Config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.security.krb5.KrbException;

/**
 * 配置redisson
 */
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() throws KrbException {
        //配置类
        Config config = new Config();
        //添加redis地址，这里添加的是单点的地址，也可以使用config.useClusterServers()添加集群地址
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        //创建客户端
        return Redisson.create(config);
    }
}
