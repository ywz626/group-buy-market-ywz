package com.ywz.domain.activity.service.trial;

import com.ywz.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.ywz.types.design.framework.tree.AbstractStrategyRouter;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/1 15:27
 */
public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> extends AbstractStrategyRouter<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext,TrialBalanceEntity> {
}
