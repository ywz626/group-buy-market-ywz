package com.ywz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * @author ywz
 */
@SpringBootApplication
@Configurable
@MapperScan("com.ywz.infrastructure.dao")
@EnableScheduling
public class Application {

    public static void main(String[] args){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        System.setProperty("user.timezone", "Asia/Shanghai");
        SpringApplication.run(Application.class);
    }

}
