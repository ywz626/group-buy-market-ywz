package com.ywz.domain.activity.service.trial.node;

import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.ywz.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.ywz.types.design.framework.tree.StrategyHandler;
import org.springframework.stereotype.Component;

/**
 * @author 于汶泽
 * @Description: 结束节点
 * @DateTime: 2025/6/1 15:24
 */
@Component
public class EndNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Override
    public TrialBalanceEntity apply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return null;
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return null;
    }
}
