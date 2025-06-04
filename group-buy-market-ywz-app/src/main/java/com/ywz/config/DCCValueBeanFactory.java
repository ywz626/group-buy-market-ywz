package com.ywz.config;

import com.ywz.types.annotations.DCCValue;
import com.ywz.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 于汶泽
 * @Description: 进行动态配置的核心类，postProcessAfterInitialization负责初始化属性的默认值
 * @DateTime: 2025/6/3 12:26
 */
@Configuration
@Slf4j
public class DCCValueBeanFactory implements BeanPostProcessor {

    private final RedissonClient redissonClient;
    private final Map<String, Object> dccObjGroup = new HashMap<>();

    public DCCValueBeanFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 在运行时动态更新 DCC 配置值
     */
    @Bean
    public RTopic dccRedisTopicListener(RedissonClient redissonClient, BeanFactory beanFactory) {
        RTopic topic = redissonClient.getTopic("group_buy_market_dcc");
        topic.addListener(String.class, (charSequence, s) -> {
            String[] split = s.split(Constants.SPLIT);

            // 获取值
            String attribute = split[0];
            String value = split[1];
            String key = Constants.BASE_CONFIG_PATH.concat(attribute);

            // 设置值
            Object bean = dccObjGroup.get(key);
            RBucket<String> bucket = redissonClient.getBucket(key);
            if(!bucket.isExists()) {
                // 如果 Redisson 中不存在该值，则不进行任何操作
                return;
            }
            bucket.set(value);
            if (bean == null) {
                // 如果没有找到对应的 bean，则不进行任何操作
                return;
            }
            Class<?> beanClass = bean.getClass();
            if(AopUtils.isAopProxy(bean)) {
                beanClass = AopUtils.getTargetClass(bean);
            }
            try {
                Field declaredField = beanClass.getDeclaredField(attribute);
                declaredField.setAccessible(true);
                // 更新属性值
                declaredField.set(bean, value);
                declaredField.setAccessible(false);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return topic;
    }

    /**
     *初始化 DCCValue 注解的属性值
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Object targetBean = bean;
        Class<?> targetClass = bean.getClass();
        // 获取代理对象的真实类
        if(AopUtils.isAopProxy(bean)){
            targetClass = AopUtils.getTargetClass(bean);
            targetBean = AopProxyUtils.getSingletonTarget(bean);
        }
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            if(!field.isAnnotationPresent(DCCValue.class)){
                continue;
            }
            String s = field.getAnnotation(DCCValue.class).value();
            if (StringUtils.isBlank(s)) {
                throw new RuntimeException(field.getName() + " @DCCValue is not config value config case 「isSwitch/isSwitch:1」");
            }
            String[] split = s.split(Constants.DCC_SPLIT);
            String attribute = split[0];
            String value = split.length == 2 ? split[1] : null;
            log.info("默认值为：{}，属性为：{}", value, attribute);
            String key = Constants.BASE_CONFIG_PATH.concat(attribute);
            try {
                if (StringUtils.isBlank(value)) {
                    throw new RuntimeException("dcc config error " + key + " is not null - 请配置默认值！");
                }
                RBucket<String> bucket = redissonClient.getBucket(key);
                boolean exists = bucket.isExists();
                if(!exists){
                    // 如果 Redisson 中不存在该值，则设置
                    bucket.set(value);
                } else {
                    // 如果 Redisson 中存在该值，则更新
                    value = bucket.get();
                }
                field.setAccessible(true);
                field.set(targetBean,value);
                field.setAccessible(false);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            // 将对象存入 dccObjGroup 中
            dccObjGroup.put(key, targetBean);
        }
        return bean;
    }
}
