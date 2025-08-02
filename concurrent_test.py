import requests
import threading
import time
from collections import defaultdict, Counter

class ConcurrentUserTest:
    def __init__(self):
        self.host = "10.147.17.199"
        self.results = []
        self.lock = threading.Lock()
        self.success_count = 0
        self.error_count = 0
        
    def create_user(self, port, thread_id):
        """向指定端口发送创建用户请求"""
        user_name = f"{port}_{thread_id}_{int(time.time() * 1000) % 100000}"
        url = f"http://{self.host}:{port}/api/users"
        user_data = {"name": user_name}
        
        try:
            response = requests.post(url, json=user_data, timeout=10)
            if response.status_code == 200:
                user = response.json()
                with self.lock:
                    self.results.append({
                        'port': port,
                        'user_id': user.get('id'),
                        'name': user.get('name')
                    })
                    self.success_count += 1
                print(f"✅ [{port}] 线程{thread_id}: '{user_name}' -> ID: {user.get('id')}")
            else:
                with self.lock:
                    self.error_count += 1
                print(f"❌ [{port}] 线程{thread_id}: 失败 {response.status_code}")
        except Exception as e:
            with self.lock:
                self.error_count += 1
            print(f"💥 [{port}] 线程{thread_id}: {str(e)}")
    
    def run_test(self, port_config):
        """运行自定义并发测试"""
        print(f"🚀 开始并发测试")
        print(f"目标服务器: {self.host}")
        print("端口配置:")
        for port, count in port_config.items():
            print(f"  端口 {port}: {count} 个并发请求")
        print("=" * 50)
        
        threads = []
        start_time = time.time()
        
        # 为每个端口创建指定数量的线程
        for port, request_count in port_config.items():
            for i in range(request_count):
                thread = threading.Thread(target=self.create_user, args=(port, i))
                threads.append(thread)
        
        # 启动所有线程
        for thread in threads:
            thread.start()
        
        # 等待所有线程完成
        for thread in threads:
            thread.join()
        
        end_time = time.time()
        
        print(f"\n✅ 测试完成! 耗时: {end_time - start_time:.2f}秒")
        print(f"📊 成功: {self.success_count}, 失败: {self.error_count}")
        
        self.analyze_results()
    
    def analyze_results(self):
        """分析结果"""
        if not self.results:
            print("❌ 没有成功的请求")
            return
        
        # 按端口分组
        port_stats = defaultdict(list)
        all_ids = []
        
        for result in self.results:
            port_stats[result['port']].append(result)
            all_ids.append(result['user_id'])
        
        print(f"\n📈 各端口统计:")
        for port in sorted(port_stats.keys()):
            records = port_stats[port]
            ids = [r['user_id'] for r in records]
            print(f"  端口 {port}: {len(records)} 条记录, ID范围: {min(ids)}-{max(ids)}")
        
        # 检查重复ID
        print(f"\n🔍 ID重复检查:")
        id_counter = Counter(all_ids)
        duplicates = {id_val: count for id_val, count in id_counter.items() if count > 1}
        
        if duplicates:
            print(f"❌ 发现 {len(duplicates)} 个重复ID:")
            for duplicate_id, count in duplicates.items():
                duplicate_ports = [r['port'] for r in self.results if r['user_id'] == duplicate_id]
                print(f"  ID {duplicate_id}: 重复{count}次, 来自端口{duplicate_ports}")
        else:
            print("✅ 没有重复ID")
        
        print(f"\n📊 总结: {len(all_ids)} 个ID, {len(set(all_ids))} 个唯一ID")

def main():
    test = ConcurrentUserTest()
    
    print("🎯 简化并发测试工具")
    print("=" * 50)
    
    # 用户自定义配置 - 端口: 并发数量
    port_config = {
        10001: 80,  # 端口10001发送80个请求
        10002: 80,  # 端口10002发送80个请求  
        10003: 80,  # 端口10003发送80个请求
        10004: 80,  # 端口10004发送80个请求
        10005: 80   # 端口10005发送80个请求
    }
    
    test.run_test(port_config)

if __name__ == "__main__":
    main()
