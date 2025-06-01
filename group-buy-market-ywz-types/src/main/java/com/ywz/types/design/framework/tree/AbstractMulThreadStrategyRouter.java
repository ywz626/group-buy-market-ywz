package com.ywz.types.design.framework.tree;

/**
 * @author 于汶泽
 * @Description: 多线程异步加载数据策略
 * @DateTime: 2025/6/1 17:33
 */
public abstract class AbstractMulThreadStrategyRouter<T, D, R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R> {
    protected final StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT;

    public R router(T requestParameter, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> strategyHandler = get(requestParameter, dynamicContext);
        if (null != strategyHandler) {
            return strategyHandler.apply(requestParameter, dynamicContext);
        }
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }

    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        multiThread(requestParameter, dynamicContext);
        return doApply(requestParameter, dynamicContext);
    }

    /**
     * 异步加载数据
     *
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @throws Exception 异常
     */
    protected abstract void multiThread(T requestParameter, D dynamicContext) throws Exception;

    /**
     * 业务流程受理
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     */
    protected abstract R doApply(T requestParameter, D dynamicContext) throws Exception;
}
