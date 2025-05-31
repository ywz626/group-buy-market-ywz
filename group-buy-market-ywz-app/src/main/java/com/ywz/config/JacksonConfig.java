package com.ywz.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 于汶泽
 * @Description: Jackson配置类
 * @DateTime: 2025/5/31 17:58
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 可以在这里进行其他的配置
        // 美化输出
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // 忽略未知属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 忽略 null 值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 支持 Java 8 时间类型
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
