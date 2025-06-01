package com.ywz.types.design.framework.tree;

/**
 * @author 于汶泽
 * @Description: 策略树操作器
 * @DateTime: 2025/6/1 15:05
 */
public interface StrategyHandler<T, D, R> {

    StrategyHandler DEFAULT = (T,D)-> null;

    /**
     * 执行策略
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 返参
     * @throws Exception 异常
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;
}
