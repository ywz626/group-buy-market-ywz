package com.ywz.domain.trade.service.lock.factory;

import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.ywz.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import com.ywz.domain.trade.service.lock.filter.TeamStockOccupyRuleFilter;
import com.ywz.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
import com.ywz.types.design.framework.link.model2.LinkArmory;
import com.ywz.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @author 于汶泽
 * @Description: 交易锁单规则过滤链工厂类
 * @DateTime: 2025/6/4 16:47
 */
@Service
public class TradeLockRuleFilterFactory {

    /**
     * 创建交易锁单规则过滤链的Bean
     *
     * @param activityUsabilityRuleFilter 活动可用性规则过滤器，用于检查活动是否可用
     * @param userTakeLimitRuleFilter 用户领取限制规则过滤器，用于检查用户领取限制
     * @param teamStockOccupyRuleFilter 团队库存占用规则过滤器，用于检查团队库存占用情况
     * @return 交易锁单规则过滤链，包含活动可用性、用户领取限制和团队库存占用的业务逻辑处理
     */
    @Bean("tradeLockRuleFilter")
    public BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter(
            ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter, TeamStockOccupyRuleFilter teamStockOccupyRuleFilter
            ) {
        // 创建链接器工厂，初始化交易锁单规则过滤链
        LinkArmory<TradeLockRuleCommandEntity, DynamicContext, TradeLockRuleFilterBackEntity> linkArmory = new LinkArmory<>("交易锁单规则过滤链"
                , activityUsabilityRuleFilter, userTakeLimitRuleFilter,teamStockOccupyRuleFilter);
        return linkArmory.getBusinessLinkedList();
    }




    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private GroupBuyActivityEntity groupBuyActivity;

        private String teamStockKey = "group_buy_market_team_stock_key_";

        private Integer userTakeOrderCount;

        /**
         * 生成团队库存键值
         *
         * @param teamId 团队ID，不能为空
         * @return 返回生成的团队库存键值，格式为：基础键值+活动ID+"_"+团队ID；如果teamId为空则返回null
         */
        public String generateTeamStockKey(String teamId) {
            // 检查团队ID是否为空，为空则直接返回null
            if (StringUtils.isBlank(teamId)) return null;
            // 拼接并返回团队库存键值
            return teamStockKey + groupBuyActivity.getActivityId() + "_" + teamId;
        }

        /**
         * 生成_recovery
         *
         * @param teamId 团队ID，不能为空
         * @return 恢复团队库存的Redis键值，格式为：基础键名+活动ID+团队ID+_recovery
         */
        public String generateRecoveryTeamStockKey(String teamId) {
            // 如果团队ID为空，则返回null
            if (StringUtils.isBlank(teamId)) return null;
            // 构造并返回恢复团队库存的Redis键值
            return teamStockKey + groupBuyActivity.getActivityId() + "_" + teamId + "_recovery";
        }
    }
}
