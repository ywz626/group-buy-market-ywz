package com.ywz.types.design.framework.link.model2.chain;

import org.apache.commons.lang3.ObjectUtils;

/**
 * @author 于汶泽
 * @Description: 策略链的链表实现
 * @DateTime: 2025/6/4 11:07
 */
public class LinkedList<E> implements ILink<E> {

    private final String name;

    transient int size = 0;

    transient Node<E> first;

    transient Node<E> last;

    public LinkedList(String name) {
        this.name = name;
    }

    void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null) {
            last = newNode;
        } else {
            f.prev = newNode;
        }
        size++;
    }

    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        size++;
    }



    @Override
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    @Override
    public boolean addFirst(E e) {
        linkFirst(e);
        return true;
    }

    @Override
    public boolean addLast(E e) {
        linkLast(e);
        return true;
    }

    private E unLink(Node<E> x){
        final E element = x.item;
        final Node<E> prev = x.prev;
        final Node<E> next = x.next;
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            next.prev = prev;
        }
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
        x.item = null;
        size--;
        return element;
    }

    @Override
    public boolean remove(Object o) {
        if(o == null){
            for(Node<E> x = first; x != null; x = x.next){
                if(x.item == null){
                    unLink(x);
                    return true;
                }
            }
        }
        for (Node<E> x = first; x != null; x = x.next) {
            if(o.equals(x.item)){
                unLink(x);
                return true;
            }
        }
        return false;
    }

    @Override
    public E get(int index) {
        return node(index).item;
    }
    Node<E> node(int index){
        Node<E> x;
        if (index < (size >> 1)) {
            x = first;
            for (int i = 0; i < index; i++) {
                x = x.next;
            }
        } else {
            x = last;
            for (int i = size - 1; i > index; i--) {
                x = x.prev;
            }
        }
        return x;
    }

    @Override
    public void printLinkList() {
        if (this.size == 0) {
            System.out.println("链表为空");
        } else {
            Node<E> temp = first;
            System.out.print("目前的列表，头节点：" + first.item + " 尾节点：" + last.item + " 整体：");
            while (temp != null) {
                System.out.print(temp.item + "，");
                temp = temp.next;
            }
            System.out.println();
        }
    }

    protected static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        public Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
