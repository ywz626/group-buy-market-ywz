package com.ywz.types.design.framework.tree;

/**
 * @author 于汶泽
 * @Description: 策略树映射器
 * @DateTime: 2025/6/1 15:04
 */
public interface StrategyMapper<T,D,R> {

    /**
     * 获取待执行策略
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 返参
     * @throws Exception 异常
     */
    StrategyHandler<T, D, R> get(T requestParameter, D dynamicContext) throws Exception;


}
