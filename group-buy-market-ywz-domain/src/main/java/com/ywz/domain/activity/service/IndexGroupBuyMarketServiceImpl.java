package com.ywz.domain.activity.service;

import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.ywz.types.design.framework.tree.StrategyHandler;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: 首页营销服务
 * @DateTime: 2025/6/1 15:35
 */
public class IndexGroupBuyMarketServiceImpl implements IIndexGroupBuyMarketService{

    @Resource
    private DefaultActivityStrategyFactory defaultFactory;

    @Override
    public TrialBalanceEntity getTrialBalance(MarketProductEntity marketProductEntity) throws Exception {
        StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> strategyHandler = defaultFactory.strategyHandler();
        return strategyHandler.apply(marketProductEntity,new DefaultActivityStrategyFactory.DynamicContext());
    }
}
