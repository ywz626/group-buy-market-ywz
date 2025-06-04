package com.ywz.domain.trade.service.factory;

import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.TradeRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.ywz.domain.trade.service.filter.ActivityUsabilityRuleFilter;
import com.ywz.domain.trade.service.filter.UserTakeLimitRuleFilter;
import com.ywz.types.design.framework.link.model2.LinkArmory;
import com.ywz.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/4 16:47
 */
@Service
public class TradeRuleFilterFactory {

    @Bean("tradeRuleFilter")
    public BusinessLinkedList<TradeRuleCommandEntity,TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> tradeRuleFilter(
            ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter
            ){
        LinkArmory<TradeRuleCommandEntity, DynamicContext, TradeRuleFilterBackEntity> linkArmory = new LinkArmory<>("交易规则过滤链"
                , activityUsabilityRuleFilter, userTakeLimitRuleFilter);
        return linkArmory.getBusinessLinkedList();
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private GroupBuyActivityEntity groupBuyActivity;

    }
}
