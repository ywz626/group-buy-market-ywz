package com.ywz.trigger.job;

import com.alibaba.fastjson.JSON;
import com.ywz.domain.trade.service.ITradeSettlementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

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

    /**
     * 定时执行拼团结算通知任务
     * 该方法通过cron表达式每分钟执行一次，用于处理拼团结算完成后的回调通知
     */
    @Scheduled(cron = "0 * * * * *")
    public void exec() {
        try {
            // 执行拼团结算通知任务并获取处理结果
            Map<String, Integer> result = tradeSettlementOrderService.execSettlementNotifyJob();
            log.info("定时任务，回调通知拼团完结任务 result:{}", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("定时任务，回调通知拼团完结任务失败", e);
        }
    }


}
