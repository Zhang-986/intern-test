package com.example.zzk.codingExp;

public class TestLRU {
    public static void main(String[] args) {
        LRUCache lru = new LRUCache(3);
        
        System.out.println("==================== 模拟过程 ====================\n");
        
        System.out.println("1️⃣  PUT(1, 'A')");
        lru.put(1, 'A');
        lru.display();  // [1:A] → END
        System.out.println();
        
        System.out.println("2️⃣  PUT(2, 'B')");
        lru.put(2, 'B');
        lru.display();  // [2:B] → [1:A] → END
        System.out.println();
        
        System.out.println("3️⃣  PUT(3, 'C')");
        lru.put(3, 'C');
        lru.display();  // [3:C] → [2:B] → [1:A] → END
        System.out.println();
        
        System.out.println("4️⃣  GET(1) - 访问键1，它变成最近使用");
        int val = lru.get(1);
        System.out.println("返回值: " + val);
        lru.display();  // [1:A] → [3:C] → [2:B] → END
        System.out.println();
        
        System.out.println("5️⃣  PUT(4, 'D') - 容量满了，删除最久未使用的(键2)");
        lru.put(4, 'D');
        lru.display();  // [4:D] → [1:A] → [3:C] → END
        System.out.println("键2被删除\n");
        
        System.out.println("6️⃣  GET(2) - 键2已不存在");
        val = lru.get(2);
        System.out.println("返回值: " + val + " (找不到)");
        lru.display();  // 无变化
        System.out.println();
        
        System.out.println("7️⃣  PUT(1, 'A2') - 更新键1的值");
        lru.put(1, 'A');
        lru.display();  // [1:A2] → [4:D] → [3:C] → END
        System.out.println();
    }
}