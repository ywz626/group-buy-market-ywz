package com.ywz.types.design.framework.tree;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/1 15:08
 */
public abstract class AbstractStrategyRouter<T,D,R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R>{

    private final StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT;

    public R router(T requestParameter, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> strategyHandler = get(requestParameter, dynamicContext);
        if (null != strategyHandler) {
            return strategyHandler.apply(requestParameter, dynamicContext);
        }
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }
}
