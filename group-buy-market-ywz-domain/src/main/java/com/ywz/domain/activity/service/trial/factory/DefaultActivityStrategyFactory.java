package com.ywz.domain.activity.service.trial.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.ywz.domain.activity.model.valobj.SkuVO;
import com.ywz.domain.activity.service.trial.node.RootNode;
import lombok.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author 于汶泽
 * @Description: 活动策略工厂
 * @DateTime: 2025/6/1 15:24
 */
@Component
public class DefaultActivityStrategyFactory {
    private final RootNode rootNode;

    public DefaultActivityStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> strategyHandler() {
        return rootNode;
    }

    @Data
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        // 可以添加动态上下文的属性和方法

        private GroupBuyActivityDiscountVO groupBuyActivityDiscountVO;
        private SkuVO skuVO;
        // 折扣金额
        private BigDecimal deductionPrice;
        // 支付金额
        private BigDecimal payPrice;
        private boolean visible;
        private boolean enable;

    }
}
