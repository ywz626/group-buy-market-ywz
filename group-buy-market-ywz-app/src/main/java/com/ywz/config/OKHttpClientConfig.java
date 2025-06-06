package com.ywz.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 于汶泽
 * @Description: OKHttpClient配置类
 * @DateTime: 2025/6/5 21:11
 */
@Configuration
public class OKHttpClientConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
}
