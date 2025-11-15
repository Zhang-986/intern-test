package com.example.zzk.codingExp;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class t2 {
    public static void main(String[] args) {
        ArrayList<String> students = new ArrayList<>();
        // 添加学生（想加多少加多少！）
        // 1. 随时扩容
        // 2. 按顺序存放元素
        // 3. 元素能够重复
        students.add("张三");
        students.add("李四");
        students.add("李四");
        students.add("王五");
        students.add("赵六");
        students.add("孙七");
        students.add("周八");
        for (int i = 0; i < students.size(); i++) {
            System.out.println(students.get(i));
        }
        // 删除学生
        System.out.println("删除学生");
        students.remove(1);
        for (int i = 0; i < students.size(); i++) {
            System.out.print(students.get(i));
        }
        System.out.println();
        System.out.println("查找学生");
        System.out.println(students.contains("王五"));
        // 修改学生
        students.set(0,"张三修改");
        for (int i = 0; i < students.size(); i++){
            System.out.print(students.get(i));
        }



    }
}