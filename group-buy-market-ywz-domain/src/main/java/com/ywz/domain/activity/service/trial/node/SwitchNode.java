package com.ywz.domain.activity.service.trial.node;

import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.ywz.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.ywz.types.design.framework.tree.StrategyHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

/**
 * @author 于汶泽
 * @Description: 开关节点
 * @DateTime: 2025/6/1 15:24
 */
@Service
public class SwitchNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Resource
    private MarketNode marketNode;

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return marketNode;
    }

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }
}
