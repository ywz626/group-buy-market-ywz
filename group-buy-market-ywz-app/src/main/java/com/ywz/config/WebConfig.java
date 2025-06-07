package com.ywz.config;

import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper mapper = new ObjectMapper();
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        mapper.setDateFormat(new SimpleDateFormat(dateFormat));
        mapper.setTimeZone(TimeZone.getDefault()); // 或者设置为 GMT+8 等

        converters.add(new MappingJackson2HttpMessageConverter(mapper));
    }
}