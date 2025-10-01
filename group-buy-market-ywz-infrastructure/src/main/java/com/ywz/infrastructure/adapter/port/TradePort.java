package com.ywz.infrastructure.adapter.port;

import com.ywz.domain.trade.adapter.port.ITradePort;
import com.ywz.domain.trade.model.entity.NotifyTaskEntity;
import com.ywz.domain.trade.model.valobj.NotifyTypeEnumVO;
import com.ywz.infrastructure.event.EventPublisher;
import com.ywz.infrastructure.gateway.NotifyRequestDTO;
import com.ywz.infrastructure.redis.IRedisService;
import com.ywz.types.enums.NotifyTaskHTTPEnumVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: Trade领域的对外接口实现
 * @DateTime: 2025/6/5 21:16
 */
@Service
public class TradePort implements ITradePort {


    @Resource
    private NotifyRequestDTO notifyRequestDTO;
    @Resource
    private IRedisService redisService;
    @Resource
    private EventPublisher publisher;

    /**
     * 包装订单支付完成后的异步回调接口
     *
     * @param notifyTask 通知任务实体，包含通知类型、URL、参数等信息
     * @return 通知结果状态码，如成功、失败或空状态
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception {
        // 获取分布式锁，防止并发处理同一任务
        RLock lock = redisService.getLock(notifyTask.lockKey());
        try {
            // 尝试获取锁，避免重复处理
            if (lock.tryLock()) {
                try {

                    // 根据通知类型执行不同的通知方式
                    if (notifyTask.getNotifyType().equals(NotifyTypeEnumVO.HTTP.getCode())) {
                        // 检查通知URL是否为空或无效，如果无效则直接返回成功
                        if (StringUtils.isBlank(notifyTask.getNotifyUrl()) || "暂无".equals(notifyTask.getNotifyUrl())) {
                            return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                        }
                        // HTTP方式通知
                        return notifyRequestDTO.groupBuyNotify(notifyTask.getNotifyUrl(), notifyTask.getParameterJson());
                    }
                    if (notifyTask.getNotifyType().equals(NotifyTypeEnumVO.MQ.getCode())) {
                        // MQ方式通知
                        publisher.publish(notifyTask.getNotifyMQ(), notifyTask.getParameterJson());
                        return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                    }
                } finally {
                    // 释放分布式锁，确保当前线程持有锁时才解锁
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
            // 获取锁失败，返回空状态码
            return NotifyTaskHTTPEnumVO.NULL.getCode();
        } catch (Exception e) {
            // 处理异常情况，中断当前线程并返回错误码
            Thread.currentThread().interrupt();
            return NotifyTaskHTTPEnumVO.ERROR.getCode();
        }
    }


}
