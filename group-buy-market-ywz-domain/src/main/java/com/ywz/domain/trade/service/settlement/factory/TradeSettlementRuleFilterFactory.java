package com.ywz.domain.trade.service.settlement.factory;

import cn.bugstack.wrench.design.framework.link.model2.LinkArmory;
import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import com.ywz.domain.trade.model.entity.GroupBuyTeamEntity;
import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.ywz.domain.trade.service.settlement.filter.EndRuleFilter;
import com.ywz.domain.trade.service.settlement.filter.OutTradeNoRuleFilter;
import com.ywz.domain.trade.service.settlement.filter.SCRuleFilter;
import com.ywz.domain.trade.service.settlement.filter.SettableRuleFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


/**
 * @author 于汶泽
 * @Description: 订单结算服务责任链工厂
 * @DateTime: 2025/6/5 15:14
 */
@Service
public class TradeSettlementRuleFilterFactory {


    @Bean("tradeSettlementRuleFilter")
    public BusinessLinkedList<TradeSettlementRuleCommandEntity, DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter(
            SCRuleFilter scRuleFilter, SettableRuleFilter settableRuleFilter, OutTradeNoRuleFilter outTradeNoRuleFilter, EndRuleFilter endRuleFilter
            ) {
        LinkArmory<TradeSettlementRuleCommandEntity, DynamicContext, TradeSettlementRuleFilterBackEntity> filter = new LinkArmory<>("交易支付订单规则过滤链"
                , scRuleFilter, outTradeNoRuleFilter, settableRuleFilter, endRuleFilter);
        return filter.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        // 可以添加一些动态上下文信息
        private MarketPayOrderEntity marketPayOrderEntity;

        private GroupBuyTeamEntity groupBuyTeamEntity;
    }
}
