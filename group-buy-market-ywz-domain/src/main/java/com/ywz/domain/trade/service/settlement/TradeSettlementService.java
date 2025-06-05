package com.ywz.domain.trade.service.settlement;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.ywz.domain.trade.model.entity.*;
import com.ywz.domain.trade.service.ITradeSettlementService;
import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 于汶泽
 * @Description: 订单支付结算服务
 * @DateTime: 2025/6/4 21:14
 */
@Service
@Slf4j
public class TradeSettlementService implements ITradeSettlementService {


    @Resource
    private ITradeRepository repository;


    @Override
    @Transactional(rollbackFor = Exception.class,timeout = 500)
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) {
        // 判断是否为锁单订单
        String outTradeNo = tradePaySuccessEntity.getOutTradeNo();
        String userId = tradePaySuccessEntity.getUserId();
        MarketPayOrderEntity marketPayOrderEntity = repository.queryMarketLockOrderByOutTradeNo(outTradeNo, userId);
        if(marketPayOrderEntity == null){
            // 未找到对应的锁单订单
            return null;
        }
        // 查询拼团组队信息
        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeam(marketPayOrderEntity.getTeamId());
        // 查询活动过期时间与拼团组队创建时间作比较
        LocalDateTime createTime = groupBuyTeamEntity.getCreateTime()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        int activityExpireTime = repository.getActivityValidTime(groupBuyTeamEntity.getActivityId());
        log.info("拼团组队创建时间: {}, 活动过期时间: {}", createTime, activityExpireTime);
        LocalDateTime expireTime = createTime.plusMinutes(activityExpireTime);

        LocalDateTime now = LocalDateTime.now();
        log.info("当前时间: {}, 拼团组队过期时间: {}", now, expireTime);
        if(now.isAfter(expireTime)){
            // TODO 过期逻辑处理 把订单删除
            throw new AppException(ResponseCode.ACTIVITY_EXPIRED);
        }
        // 构建拼团结算聚合根
        GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate = GroupBuyTeamSettlementAggregate.builder()
                .userEntity(UserEntity.builder().userId(tradePaySuccessEntity.getUserId()).build())
                .groupBuyTeamEntity(groupBuyTeamEntity)
                .tradePaySuccessEntity(tradePaySuccessEntity)
                .build();

        // 更新拼团组队状态为已完成
        repository.settlementMarketPayOrder(groupBuyTeamSettlementAggregate);

        return TradePaySettlementEntity.builder()
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .userId(tradePaySuccessEntity.getUserId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .teamId(groupBuyTeamEntity.getTeamId())
                .channel(tradePaySuccessEntity.getChannel())
                .source(tradePaySuccessEntity.getSource())
                .build();

    }
}
