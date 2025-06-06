package com.ywz.domain.trade.service.lock.factory;

import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.ywz.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import com.ywz.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
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
 * @Description: 交易锁单规则过滤链工厂类
 * @DateTime: 2025/6/4 16:47
 */
@Service
public class TradeLockRuleFilterFactory {

    @Bean("tradeLockRuleFilter")
    public BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter(
            ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter
            ){
        LinkArmory<TradeLockRuleCommandEntity, DynamicContext, TradeLockRuleFilterBackEntity> linkArmory = new LinkArmory<>("交易锁单规则过滤链"
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
