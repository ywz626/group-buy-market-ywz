package com.ywz.domain.trade.service.settlement;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.ywz.domain.trade.model.entity.*;
import com.ywz.domain.trade.service.ITradeSettlementService;
import com.ywz.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author 于汶泽
 * @Description: 订单支付结算服务
 * @DateTime: 2025/6/4 21:14
 */
@Service
@Slf4j
public class TradeSettlementOrderService implements ITradeSettlementService {


    @Resource
    private ITradeRepository repository;

    @Resource(name = "tradeSettlementRuleFilter")
    BusinessLinkedList<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter;


    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 500)
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception {

        TradeSettlementRuleFilterBackEntity tradeSettlementRuleFilterBackEntity = tradeSettlementRuleFilter.apply(TradeSettlementRuleCommandEntity.builder()
                .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
                .channel(tradePaySuccessEntity.getChannel())
                .source(tradePaySuccessEntity.getSource())
                .userId(tradePaySuccessEntity.getUserId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .build(), new TradeSettlementRuleFilterFactory.DynamicContext());

        GroupBuyTeamEntity groupBuyTeamEntity = GroupBuyTeamEntity.builder()
                .status(tradeSettlementRuleFilterBackEntity.getStatus())
                .targetCount(tradeSettlementRuleFilterBackEntity.getTargetCount())
                .completeCount(tradeSettlementRuleFilterBackEntity.getCompleteCount())
                .validEndTime(tradeSettlementRuleFilterBackEntity.getValidEndTime())
                .activityId(tradeSettlementRuleFilterBackEntity.getActivityId())
                .validStartTime(tradeSettlementRuleFilterBackEntity.getValidStartTime())
                .createTime(new Date())
                .teamId(tradeSettlementRuleFilterBackEntity.getTeamId())
                .build();

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
