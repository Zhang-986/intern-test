package com.example.zzk.codingExp;

import java.util.*;

public class ListCRUD {
    public static void main(String[] args) {
        // Create - 创建集合
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        // Create - 添加元素
        list.add("E");                    // 末尾添加
        list.add(2, "X");                 // 指定位置插入
        list.addAll(Arrays.asList("F", "G")); // 添加多个元素
        
        // Read - 读取元素
        System.out.println("集合内容: " + list);
        System.out.println("索引2的元素: " + list.get(2));
        System.out.println("元素C的索引: " + list.indexOf("C"));
        System.out.println("集合大小: " + list.size());
        
        // Update - 更新元素
        list.set(1, "B_Updated");         // 更新索引1的元素
        
        // Delete - 删除元素
        list.remove(3);                    // 按索引删除
        list.remove("X");                 // 按元素值删除
        list.removeIf(s -> s.startsWith("F")); // 条件删除
        
        System.out.println("最终结果: " + list);
        
        // 遍历方式
        System.out.println("\n遍历方式:");
        // 1. 增强for循环
        for (String item : list) {
            System.out.print(item + " ");
        }
        
        // 2. 迭代器
        System.out.println("\n使用迭代器:");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        
        // 3. forEach方法
        System.out.println("\n使用forEach:");
        list.forEach(item -> System.out.print(item + " "));
        
        // 4. Stream API
        System.out.println("\n使用Stream:");
        list.stream().forEach(System.out::print);
    }
}