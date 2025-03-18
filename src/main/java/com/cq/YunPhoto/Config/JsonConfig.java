package com.cq.YunPhoto.Config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * 全局Json配置
 */

@JsonComponent
public class JsonConfig {


    /**
     * long转json精度丢失问题
     * @return
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册自定义序列化器
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, new JsonSerializer<Long>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.toString());
            }
        });
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
