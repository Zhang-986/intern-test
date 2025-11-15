package com.example.zzk.codingExp;

import java.util.HashMap;
import java.util.Map;
// latest recently used
// 最少使用算法
 class Node {
    int key;
    int value;
    Node prev;
    Node next;
    
    Node(int key, int value) {
        this.key = key;
        this.value = value;
    }
}

class LRUCache {
    private int capacity;
    private Map<Integer, Node> cache;  // 哈希表：key -> Node
    private Node head;                  // 虚拟头节点（最近使用）
    private Node tail;                  // 虚拟尾节点（最久未使用）
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.head = new Node(0, 0);
        this.tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }
    
    // 从链表删除节点
    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    // 添加节点到头部（最近使用）
    private void addToHead(Node node) {
        // 设置node的前后指针
        node.prev = head;
        node.next = head.next;

        // 调整相邻节点的前后指针
        head.next.prev= node;
        head.next = node;
    }
    
    // 把节点移到头部
    private void moveToHead(Node node) {
        // 将左右两边的节点连接起来
        removeNode(node);
        // 添加到头部
        addToHead(node);
    }
    
    // 获取值
    public int get(int key) {
        Node node = cache.get(key);
        if (node == null) {
            return -1;
        }
        moveToHead(node);
        return node.value;
    }
    
    // 存储值
    public void put(int key, int value) {
        Node node = cache.get(key);
        if (node == null) {
            node = new Node(key,value);
            // 记录一下
            cache.put(key, node);
            // 移动到最前边
            addToHead(node);
            if(cache.size()>capacity){
                // 删除最久未使用的节点
                Node last=  tail.prev;
                removeNode(last);
                cache.remove(last.key);
            }
        }else{
            node.value = value;
            moveToHead(node);
        }
    }
    
    // 打印缓存状态
    public void display() {
        System.out.print("缓存状态: ");
        Node current = head.next;
        while (current != tail) {
            System.out.print("[" + current.key + ":" + current.value + "] → ");
            current = current.next;
        }
        System.out.println("END");
    }
}