# Race Condition Test - Python Implementation

## Overview

This document provides comprehensive instructions for using the Python-based race condition tester, which is equivalent to the PowerShell `race-test-fixed.ps1` script but offers cross-platform compatibility and enhanced features.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Command Line Options](#command-line-options)
- [Test Scenarios](#test-scenarios)
- [Understanding Results](#understanding-results)
- [Troubleshooting](#troubleshooting)
- [Examples](#examples)

## Prerequisites

### System Requirements
- **Python**: Version 3.6 or higher
- **Network Access**: Connection to the Spring Boot application
- **Valid Credentials**: Login access to the application

### Application Requirements
- Spring Boot application running on `http://localhost:8080` (default)
- Valid user account (default: `alice@secchamp.com` / `password123`)
- Book with ID `1` exists in the database with available stock

## Installation

### 1. Install Python Dependencies

```bash
# Navigate to the script directory
cd sourcecodes/springboot-chal

# Install required packages
pip install -r requirements.txt
```

**requirements.txt contents:**
```
requests>=2.25.0
```

### 2. Verify Installation

```bash
# Check Python version
python --version

# Verify requests library
python -c "import requests; print(f'Requests version: {requests.__version__}')"
```

## Usage

### Basic Usage

```bash
# Run with default settings
python race_test_fixed.py
```

**Default Configuration:**
- Threads: 15
- Purchases per thread: 3
- URL: http://localhost:8080
- Book ID: 1
- Email: alice@secchamp.com
- Password: password123

### Advanced Usage

```bash
# Custom thread count and purchases
python race_test_fixed.py --threads 20 --purchases 5

# Different server or credentials
python race_test_fixed.py --url http://localhost:9090 --email user@test.com --password mypass

# Test specific book
python race_test_fixed.py --book-id 2
```

## Command Line Options

| Option | Short | Type | Default | Description |
|--------|-------|------|---------|-------------|
| `--threads` | `-t` | int | 15 | Number of concurrent threads |
| `--purchases` | `-p` | int | 3 | Number of purchases per thread |
| `--url` | `-u` | str | http://localhost:8080 | Base URL of application |
| `--book-id` | `-b` | str | "1" | Book ID to test with |
| `--email` | `-e` | str | alice@secchamp.com | Login email |
| `--password` | `-w` | str | password123 | Login password |

### Help Command

```bash
# View all available options
python race_test_fixed.py --help
```

## Test Scenarios

### 1. Basic Race Condition Test

**Purpose**: Detect if multiple concurrent purchases can cause negative stock

```bash
python race_test_fixed.py
```

**Expected Outcome**: 
- ✅ **Vulnerable endpoint**: Stock goes negative (race condition detected)
- ❌ **Secure endpoint**: Stock never goes below 0

### 2. High Concurrency Test

**Purpose**: Stress test with maximum concurrent load

```bash
python race_test_fixed.py --threads 50 --purchases 10
```

**Expected Outcome**: Higher chance of detecting race conditions

### 3. Specific Book Testing

**Purpose**: Test different books with varying stock levels

```bash
# Test book with low stock
python race_test_fixed.py --book-id 1 --threads 10 --purchases 2

# Test book with high stock
python race_test_fixed.py --book-id 2 --threads 20 --purchases 5
```

### 4. Different Server Testing

**Purpose**: Test against different environments

```bash
# Development server
python race_test_fixed.py --url http://dev-server:8080

# Staging server  
python race_test_fixed.py --url https://staging.example.com

# Local server on different port
python race_test_fixed.py --url http://localhost:9090
```

## Understanding Results

### Output Format

```
🧪 Race Condition Tester - Python Version
==================================================

🚀 Race Condition Test - 15 threads x 3 purchases each
📍 Using DIRECT race test endpoint for maximum race condition potential

✅ Login OK, token received

⚡ Starting 15 concurrent threads, each making 3 purchases...

📊 Results:
Total Purchases Attempted: 45
✅ Successful: 38
❌ Failed: 7

📈 Stock levels seen: 15, 12, 8, 5, 2, 0, -1, -3
📊 Stock range: 15 → -3

🚨 RACE CONDITION SUCCESS! Negative stock achieved: -3
🎯 This proves the vulnerability allows overselling!
```

### Result Interpretation

#### ✅ Successful Test (Race Condition Detected)
- **Negative Stock**: Final stock < 0 indicates overselling
- **Stock Range**: Shows progression from initial stock to negative values
- **Multiple Stock Levels**: Different threads see different stock values

#### ⚠️ Inconclusive Test
- **Stock Stays at 0**: May indicate insufficient concurrency
- **High Failure Rate**: Network or server issues
- **No Stock Changes**: Possible authentication or endpoint problems

#### ❌ Failed Test (No Race Condition)
- **Stock Never Negative**: Proper synchronization prevents overselling
- **Consistent Stock Levels**: All threads see coordinated stock updates

### Error Analysis

The script provides detailed error reporting:

```
🔍 Error Analysis:
  12x: HTTP 400
  3x: Connection timeout
  2x: HTTP 401

📋 First 3 detailed errors:
  Thread 5 Purchase 2: HTTP 400
    Details: Insufficient stock
  Thread 8 Purchase 1: Connection timeout
    Details: Request exception occurred
  Thread 12 Purchase 3: HTTP 401
    Details: Invalid token
```

## Troubleshooting

### Common Issues

#### 1. Connection Refused
```
❌ Login failed: HTTPConnectionPool(host='localhost', port=8080): Max retries exceeded
```

**Solutions:**
- Ensure Spring Boot application is running
- Verify correct URL with `--url` parameter
- Check firewall settings

#### 2. Authentication Failed
```
❌ Login failed: 401 Client Error: Unauthorized
```

**Solutions:**
- Verify credentials with `--email` and `--password`
- Check if user account exists in database
- Ensure account is not locked or disabled

#### 3. Book Not Found
```
⚠️ No stock levels recorded due to all failures
🔧 Check server status and book ID 1 exists with stock > 0
```

**Solutions:**
- Verify book exists with `--book-id` parameter
- Check initial stock level in database
- Reset database if needed

#### 4. Python Import Errors
```
ModuleNotFoundError: No module named 'requests'
```

**Solutions:**
```bash
pip install requests
# or
pip install -r requirements.txt
```

### Database Reset

If stock becomes negative and needs reset:

```bash
# Windows
reset-database.bat

# Linux/Mac
./reset-database.sh

# PowerShell
./reset-database.ps1
```

## Examples

### Example 1: Quick Vulnerability Check

```bash
# Fast test with moderate concurrency
python race_test_fixed.py --threads 10 --purchases 2
```

### Example 2: Comprehensive Testing

```bash
# Thorough test with high concurrency
python race_test_fixed.py --threads 25 --purchases 4
```

### Example 3: Production Environment Testing

```bash
# Test against production-like environment
python race_test_fixed.py \
  --url https://prod-api.example.com \
  --email prod-user@company.com \
  --password secure-password \
  --threads 5 \
  --purchases 2
```

### Example 4: Automated Testing Script

Create a batch testing script:

```bash
#!/bin/bash
# test_all_scenarios.sh

echo "Testing different concurrency levels..."

for threads in 5 10 15 20 25; do
  echo "Testing with $threads threads:"
  python race_test_fixed.py --threads $threads --purchases 3
  echo "----------------------------------------"
  sleep 2
done
```

## Comparison with PowerShell Version

| Feature | PowerShell | Python |
|---------|------------|--------|
| **Platform** | Windows only | Cross-platform |
| **Threading** | Start-Job | ThreadPoolExecutor |
| **Error Handling** | Basic | Enhanced with grouping |
| **Output Format** | Text-based | Emoji + structured |
| **Configuration** | Parameters | Command line args |
| **Dependencies** | Built-in | requests library |
| **Performance** | Good | Optimized threading |

## Security Notes

⚠️ **Important Security Considerations:**

1. **Credentials**: Never hardcode production credentials
2. **Testing Environment**: Use dedicated test environments
3. **Rate Limiting**: Be mindful of server rate limits
4. **Data Integrity**: Reset test data between runs
5. **Monitoring**: Monitor server resources during testing

## Additional Resources

- **PowerShell Version**: `race-test-fixed.ps1`
- **Requirements File**: `requirements.txt`
- **Database Reset**: `reset-database.*` scripts
- **Application Docs**: `README.md`

---

**Last Updated**: September 2025  
**Script Version**: 1.0  
**Compatibility**: Python 3.6+
