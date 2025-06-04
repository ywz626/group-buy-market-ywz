package com.ywz.types.design.framework.link.model1;

/**
 * @author 于汶泽
 * @Description: 责任链装配
 * @DateTime: 2025/6/4 10:53
 */
public interface ILogicChainLink<T, D, R> {

    ILogicLink<T, D, R> next();

    ILogicLink<T, D, R> appendNext(ILogicLink<T, D, R> next);
}
