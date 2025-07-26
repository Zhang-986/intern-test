#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简单的接口性能对比测试
"""

import requests
import time
import json

def test_performance():
    """测试串行vs并行处理性能"""
    
    # 服务器地址
    base_url = "http://localhost:8080/api/performance"
    
    # 测试数据
    test_data = {
        "id": 1001,
        "name": "性能测试",
        "category": "测试",
        "tags": ["test"],
        "amount": 1000.0
    }
    
    print("🚀 Java接口性能对比测试")
    print("=" * 50)
    
    # 检查服务器连接
    try:
        response = requests.get(f"{base_url}/test-data", timeout=5)
        if response.status_code != 200:
            print("❌ 服务器连接失败，请先启动Spring Boot应用")
            return
        print("✅ 服务器连接正常")
    except:
        print("❌ 无法连接服务器，请确保Spring Boot应用在运行")
        print("   启动命令: mvn spring-boot:run")
        return
    
    print("\n开始测试...")
    
    # 测试串行处理
    print("\n1️⃣ 测试串行处理...")
    serial_times = []
    for i in range(5):
        start_time = time.time()
        try:
            response = requests.post(
                f"{base_url}/process/serial",
                json=test_data,
                headers={"Content-Type": "application/json"},
                timeout=30
            )
            end_time = time.time()
            
            if response.status_code == 200:
                result = response.json()
                server_time = result['data']['processingTimeMs']
                client_time = (end_time - start_time) * 1000
                serial_times.append(server_time)
                print(f"   第{i+1}次: 服务端 {server_time}ms, 客户端 {client_time:.0f}ms")
            else:
                print(f"   第{i+1}次: 请求失败 {response.status_code}")
        except Exception as e:
            print(f"   第{i+1}次: 错误 {e}")
    
    # 测试并行处理
    print("\n2️⃣ 测试并行处理...")
    parallel_times = []
    for i in range(5):
        start_time = time.time()
        try:
            response = requests.post(
                f"{base_url}/process/parallel",
                json=test_data,
                headers={"Content-Type": "application/json"},
                timeout=30
            )
            end_time = time.time()
            
            if response.status_code == 200:
                result = response.json()
                server_time = result['data']['processingTimeMs']
                client_time = (end_time - start_time) * 1000
                parallel_times.append(server_time)
                print(f"   第{i+1}次: 服务端 {server_time}ms, 客户端 {client_time:.0f}ms")
            else:
                print(f"   第{i+1}次: 请求失败 {response.status_code}")
        except Exception as e:
            print(f"   第{i+1}次: 错误 {e}")
    
    # 计算平均值和性能对比
    if serial_times and parallel_times:
        serial_avg = sum(serial_times) / len(serial_times)
        parallel_avg = sum(parallel_times) / len(parallel_times)
        improvement = ((serial_avg - parallel_avg) / serial_avg) * 100
        
        print("\n📊 性能对比结果:")
        print("-" * 50)
        print(f"串行处理平均耗时:   {serial_avg:.0f} ms")
        print(f"并行处理平均耗时:   {parallel_avg:.0f} ms")
        print(f"响应时间减少:       {serial_avg - parallel_avg:.0f} ms")
        print(f"性能提升:          {improvement:.1f}%")
        
        if improvement > 0:
            print(f"\n🎉 并行处理比串行处理快 {improvement:.1f}%!")
        else:
            print(f"\n⚠️ 并行处理反而慢了 {abs(improvement):.1f}%")
            
        print("\n💡 技术说明:")
        print("- 串行处理: 验证(100ms) + 查询(200ms) + API(300ms) + 计算(150ms) = ~750ms")
        print("- 并行处理: 使用CompletableFuture并行执行，理论耗时约为最长任务时间(~300ms)")
    else:
        print("\n❌ 测试失败，无法获取有效数据")

if __name__ == "__main__":
    test_performance()
    input("\n按Enter键退出...")
