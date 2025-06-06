package com.ywz.types.design.framework.link.model2;

import com.ywz.types.design.framework.link.model2.chain.BusinessLinkedList;
import com.ywz.types.design.framework.link.model2.handler.ILogicLinkHandler;

/**
 * @author 于汶泽
 * @Description: 简化链式处理实现多例，提供一个武器库，存放业务链表
 * @DateTime: 2025/6/4 12:15
 */
public class LinkArmory<T,D,R>{

    private final BusinessLinkedList<T, D, R> businessLinkedList;

    @SafeVarargs
    public LinkArmory(String name, ILogicLinkHandler<T,D,R>... handler){
        businessLinkedList = new BusinessLinkedList<>(name);
        for (ILogicLinkHandler<T, D, R> logicLinkHandler : handler) {
            businessLinkedList.add(logicLinkHandler);
        }
    }

    public BusinessLinkedList<T, D, R> getBusinessLinkedList() {
        return businessLinkedList;
    }
}
