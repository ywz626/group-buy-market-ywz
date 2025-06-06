package com.ywz.types.design.framework.link.model2.handler;

/**
 * @author 于汶泽
 * @Description: 策略链的控制器
 * @DateTime: 2025/6/4 12:11
 */
public interface ILogicLinkHandler<T, D, R> {

    default R next(T requestParameter, D dynamicContext) {
        return null;
    }

    R apply(T requestParameter, D dynamicContext) throws Exception;
}
