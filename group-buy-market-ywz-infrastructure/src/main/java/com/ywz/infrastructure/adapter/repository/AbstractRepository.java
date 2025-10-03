package com.ywz.infrastructure.adapter.repository;


import com.ywz.infrastructure.dcc.DCCService;
import com.ywz.infrastructure.redis.RedissonService;
import com.ywz.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-03
 * @Description: 仓储服务的抽象类
 * @Version: 1.0
 */
@Slf4j
public abstract class AbstractRepository {

    @Resource
    private RedissonService redissonService;
    @Resource
    private DCCService dccService;

    /**
     * 从缓存或数据库中获取数据
     * <p>
     * 该方法首先检查缓存开关是否开启，如果开启则优先从Redis缓存中获取数据，
     * 如果缓存中没有则通过分布式锁机制从数据库加载数据并更新到缓存中。
     * 如果缓存开关关闭，则直接从数据库获取数据。
     *
     * @param key         缓存键值，用于标识缓存中的数据
     * @param dbSupplier  数据库数据提供者，当缓存中没有数据时调用此供应器从数据库获取数据
     * @param <T>         返回数据的泛型类型
     * @return 从缓存或数据库中获取的数据，如果数据不存在则返回null
     */
    protected <T> T getFromCacheOrDb(String key, Supplier<T> dbSupplier) {
        // 检查缓存开关是否开启
        if (dccService.isCacheOpenSwitch()) {
            // 首先尝试从缓存中获取数据
            T cacheValue = redissonService.getValue(key);
            if (cacheValue != null) {
                // 处理缓存中的空值标记
                if (cacheValue == Constants.GROUP_BUY_MARKET_NULL) {
                    return null;
                }
                return cacheValue;
            }

            // 缓存中没有数据，需要从数据库加载
            String lockKey = Constants.CACHE_LOCK + key;
            RLock lock = redissonService.getLock(lockKey);
            boolean tryLock = false;
            try {
                // 尝试获取分布式锁，避免缓存击穿
                tryLock = lock.tryLock(2000, TimeUnit.MILLISECONDS);
                if (!tryLock) {
                    // 获取锁失败，进行重试
                    log.error("获取锁失败,开始重试");
                    Thread.sleep(500);
                    return getFromCacheOrDb(key, dbSupplier);
                }

                // 再次检查缓存，防止重复加载
                cacheValue = redissonService.getValue(key);
                if (cacheValue != null) {
                    if (cacheValue == Constants.GROUP_BUY_MARKET_NULL) {
                        return null;
                    }
                    return cacheValue;
                }

                // 从数据库中获取数据
                log.info("从数据库中获取数据");
                T dbValue = dbSupplier.get();
                if (dbValue == null) {
                    // 数据库中也没有数据，设置空值标记到缓存中
                    redissonService.setValue(key, Constants.GROUP_BUY_MARKET_NULL, 10 * 60 * 1000);
                    return null;
                }

                // 将数据库获取的数据存入缓存，设置30分钟过期时间
                redissonService.setValue(key, dbValue, 30 * 60 * 1000);
                return dbValue;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                // 释放分布式锁
                if (tryLock) {
                    lock.unlock();
                }
            }
        } else {
            // 缓存开关关闭，直接从数据库获取数据
            log.warn("缓存降级，从数据库中获取数据");
            return dbSupplier.get();
        }
    }
}
