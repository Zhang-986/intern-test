package com.example.zzk.codingExp;

import java.util.Scanner;

public class MethodDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Java方法体验演示 ===");
        System.out.println("1. 无方法的实现（代码冗余）");
        System.out.println("2. 有方法的实现（简洁高效）");
        System.out.print("请选择演示模式: ");
        
        int mode = scanner.nextInt();
        
        if (mode == 1) {
            demonstrateWithoutMethods();
        } else {
            demonstrateWithMethods();
        }
        
        scanner.close();
    }
    
    // 无方法的实现 - 大量重复代码
    public static void demonstrateWithoutMethods() {
        System.out.println("\n--- 无方法的实现 ---");
        
        // 第一个学生成绩计算
        int[] scores1 = {85, 90, 78, 92, 88};
        int sum1 = 0;
        for (int i = 0; i < scores1.length; i++) {
            sum1 += scores1[i];
        }
        double average1 = (double) sum1 / scores1.length;
        System.out.println("学生1的平均分: " + average1);
        
        // 第二个学生成绩计算 - 重复代码！
        int[] scores2 = {76, 82, 95, 88, 79};
        int sum2 = 0;
        for (int i = 0; i < scores2.length; i++) {
            sum2 += scores2[i];
        }
        double average2 = (double) sum2 / scores2.length;
        System.out.println("学生2的平均分: " + average2);
        
        // 第三个学生成绩计算 - 再次重复！
        int[] scores3 = {91, 87, 83, 90, 85};
        int sum3 = 0;
        for (int i = 0; i < scores3.length; i++) {
            sum3 += scores3[i];
        }
        double average3 = (double) sum3 / scores3.length;
        System.out.println("学生3的平均分: " + average3);
        
        System.out.println("\n问题分析:");
        System.out.println("1. 相同逻辑重复3次，代码冗余");
        System.out.println("2. 修改计算逻辑需要改多处");
        System.out.println("3. 容易出错，维护困难");
    }
    
    // 有方法的实现 - 简洁高效
    public static void demonstrateWithMethods() {
        System.out.println("\n--- 有方法的实现 ---");
        
        int[] scores1 = {85, 90, 78, 92, 88};
        int[] scores2 = {76, 82, 95, 88, 79};
        int[] scores3 = {91, 87, 83, 90, 85};
        
        // 使用方法，代码简洁清晰
        System.out.println("学生1的平均分: " + calculateAverage(scores1));
        System.out.println("学生2的平均分: " + calculateAverage(scores2));
        System.out.println("学生3的平均分: " + calculateAverage(scores3));
        
        // 展示更多方法优势
        System.out.println("\n学生1的成绩等级: " + getGradeLevel(calculateAverage(scores1)));
        System.out.println("学生2的成绩等级: " + getGradeLevel(calculateAverage(scores2)));
        System.out.println("最高分: " + findMaxScore(scores1, scores2, scores3));
        
        System.out.println("\n方法带来的好处:");
        System.out.println("1. 代码复用，减少冗余");
        System.out.println("2. 逻辑封装，易于维护");
        System.out.println("3. 职责分离，易于测试");
        System.out.println("4. 提高可读性");
    }
    
    // 计算平均分的方法
    public static double calculateAverage(int[] scores) {
        int sum = 0;
        for (int score : scores) {
            sum += score;
        }
        return (double) sum / scores.length;
    }
    
    // 根据平均分获取等级
    public static String getGradeLevel(double average) {
        if (average >= 90) return "优秀";
        else if (average >= 80) return "良好";
        else if (average >= 70) return "中等";
        else if (average >= 60) return "及格";
        else return "不及格";
    }
    
    // 找出最高分
    public static int findMaxScore(int[]... scoresArrays) {
        int max = Integer.MIN_VALUE;
        for (int[] scores : scoresArrays) {
            for (int score : scores) {
                if (score > max) {
                    max = score;
                }
            }
        }
        return max;
    }
}