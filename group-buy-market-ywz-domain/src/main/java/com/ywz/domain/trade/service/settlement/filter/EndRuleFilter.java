package com.ywz.domain.trade.service.settlement.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import com.ywz.domain.trade.model.entity.GroupBuyTeamEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.ywz.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import org.springframework.stereotype.Service;

/**
 * @author 于汶泽
 * @Description: 订单支付结算规则过滤器 - 整合对象的结束节点
 * @DateTime: 2025/6/5 15:13
 */
@Service
public class EndRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    /**
     * 应用团购团队信息到结算规则过滤器返回实体
     *
     * @param requestParameter 结算规则命令实体参数
     * @param dynamicContext 动态上下文，包含团购团队信息
     * @return 包含团购团队详细信息的结算规则过滤器返回实体
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 从动态上下文中获取团购团队实体信息
        GroupBuyTeamEntity groupBuyTeamEntity = dynamicContext.getGroupBuyTeamEntity();

        // 构建并返回包含团购团队信息的结算规则过滤器返回实体
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
                .notifyConfigVO(groupBuyTeamEntity.getNotifyConfig())
                .build();
    }

}
