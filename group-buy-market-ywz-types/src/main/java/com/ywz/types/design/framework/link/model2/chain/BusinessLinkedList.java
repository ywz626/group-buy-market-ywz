package com.ywz.types.design.framework.link.model2.chain;

import com.ywz.types.design.framework.link.model1.ILogicLink;
import com.ywz.types.design.framework.link.model2.handler.ILogicLinkHandler;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/4 12:06
 */
public class BusinessLinkedList<T,D,R> extends LinkedList<ILogicLinkHandler<T, D, R>> implements ILogicLinkHandler<T,D,R> {

    public BusinessLinkedList(String name) {
        super(name);
    }

    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        Node<ILogicLinkHandler<T, D, R>> current = first;
        do {
            ILogicLinkHandler<T, D, R>  item = current.item;
            R apply = item.apply(requestParameter, dynamicContext);
            if(apply != null) {
                return apply;
            }

            current = current.next;
        }while(current != null);

        return null;
    }
}
