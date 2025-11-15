package com.example.zzk.codingExp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class t4 {
    public static void main(String[] args) {
        HashMap<String,Integer> map = new HashMap<>();
        // 1. 基于hashset来构建的Key
        map.put("张三",100);
        map.put("李四",95);
        map.put("王五",80);
        map.put("赵六",75);
        map.remove("李四");
        map.put("李四",100);
        System.out.println(map.containsKey("王五"));
        map.forEach((k,v)-> System.out.println(k+":"+v));

    }
}
