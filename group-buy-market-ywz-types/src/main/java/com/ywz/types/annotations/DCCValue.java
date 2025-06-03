package com.ywz.types.annotations;

import java.lang.annotation.*;

/**
 * @author 于汶泽
 * @Description: 动态配置注解
 * @DateTime: 2025/6/3 12:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DCCValue {
    String value() default "";
}
