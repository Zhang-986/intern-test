package com.example.zzk.codingExp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class t3 {
    public static void main(String[] args) {
        // Set集合
        // 1. 去重特性
        // 2. 不按顺序
        HashSet<String> set = new HashSet<>();
        set.add("张三");
        set.add("张三");
        set.add("张三");
        set.add("张三");
        set.add("李四");
        for (String s : set) {
            System.out.println(s);
        }



    }
}
