package com.ywz.domain.trade.service.settlement.filter;

import com.ywz.domain.trade.model.entity.GroupBuyTeamEntity;
import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.ywz.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.handler.ILogicLinkHandler;
import org.springframework.stereotype.Service;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/5 15:13
 */
@Service
public class EndRuleFilter implements ILogicLinkHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyTeamEntity groupBuyTeamEntity = dynamicContext.getGroupBuyTeamEntity();

        return TradeSettlementRuleFilterBackEntity.builder()
                .validEndTime(groupBuyTeamEntity.getValidEndTime())
                .targetCount(groupBuyTeamEntity.getTargetCount())
                .completeCount(groupBuyTeamEntity.getCompleteCount())
                .lockCount(groupBuyTeamEntity.getLockCount())
                .status(groupBuyTeamEntity.getStatus())
                .teamId(groupBuyTeamEntity.getTeamId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .validStartTime(groupBuyTeamEntity.getValidStartTime())
                .completeCount(groupBuyTeamEntity.getCompleteCount())
                .build();
    }
}
