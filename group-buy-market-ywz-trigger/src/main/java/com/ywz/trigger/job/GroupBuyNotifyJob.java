package com.ywz.trigger.job;

import com.alibaba.fastjson.JSON;
import com.ywz.domain.trade.service.ITradeSettlementService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 拼团完结回调通知任务；拼团回调任务表，实际公司场景会定时清理数据结转，不会有太多数据挤压
 * @create 2025-01-31 10:27
 */
@Slf4j
@Service
public class GroupBuyNotifyJob {

    @Resource
    private ITradeSettlementService tradeSettlementOrderService;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 定时执行拼团结算通知任务
     * 该方法通过cron表达式每分钟执行一次，用于处理拼团结算完成后的回调通知
     */
    @Scheduled(cron = "0 * * * * *")
    public void exec() {
        // 获取分布式锁，防止多个实例同时执行任务
        RLock lock = redissonClient.getLock("group_buy_market_notify_job_exec");
        try {
            // 尝试获取锁，等待时间为3秒，租期为0秒（自动续期）
            boolean isLock = lock.tryLock(3, 0, TimeUnit.SECONDS);
            if(!isLock) return;
            // 执行拼团结算通知任务并获取处理结果
            Map<String, Integer> result = tradeSettlementOrderService.execSettlementNotifyJob();
            log.info("定时任务，回调通知拼团完结任务 result:{}", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("定时任务，回调通知拼团完结任务失败", e);
        }finally {
            // 释放分布式锁，确保当前线程持有锁且锁仍处于锁定状态
            if(lock.isHeldByCurrentThread() && lock.isLocked()){
                lock.unlock();
            }
        }
    }

}
