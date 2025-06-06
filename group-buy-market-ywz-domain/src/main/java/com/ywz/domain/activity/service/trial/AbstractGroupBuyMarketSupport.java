package com.ywz.domain.activity.service.trial;

import com.ywz.domain.activity.adapter.repository.IActivityRepository;
import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.ywz.types.design.framework.tree.AbstractMulThreadStrategyRouter;
import com.ywz.types.design.framework.tree.AbstractStrategyRouter;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author 于汶泽
 * @Description: 多线程抽象路由在拼团活动试算服务中的支持类  这里泛型不同代表不同的业务场景
 * @DateTime: 2025/6/1 15:27
 */
public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity, DynamicContext, TrialBalanceEntity> extends AbstractMulThreadStrategyRouter<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext,TrialBalanceEntity> {
    @Resource
    protected IActivityRepository repository;

    protected final int timeout = 2;

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        // 缺省的方法
    }
}
