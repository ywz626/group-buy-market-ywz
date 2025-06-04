package com.ywz.types.design.framework.link.model1;

/**
 * @author 于汶泽
 * @Description: 责任链链路接口
 * @DateTime: 2025/6/4 10:52
 */
public interface ILogicLink<T, D, R> extends ILogicChainLink<T, D, R>{

    R apply(T requestParameter, D dynamicContext) throws Exception;

}
