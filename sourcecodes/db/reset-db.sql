-- Reset database script - drops and recreates the entire database
-- This ensures a clean state for development and testing
-- Run this when you need to completely reset the database

DROP DATABASE IF EXISTS secchamp2025;
CREATE DATABASE secchamp2025;
USE secchamp2025;

-- Create Users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    address TEXT,
    role VARCHAR(20) DEFAULT 'USER',
    is_admin BOOLEAN DEFAULT FALSE,
    account_status VARCHAR(20) DEFAULT 'ACTIVE',
    credit_limit DECIMAL(10,2) DEFAULT 100.00,
    newsletter BOOLEAN DEFAULT TRUE,
    promotions BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Books table
CREATE TABLE IF NOT EXISTS books (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category VARCHAR(50),
    price DECIMAL(10,2),
    description TEXT,
    stock INT DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 0.00,
    image_url VARCHAR(500),
    publisher VARCHAR(100),
    publish_date VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample users with different passwords for testing
-- Alice: password123 -> $2a$10$8.H0Q1J5WCZD7kF6HCXXpO0OTgLt8kGU5ZJ5rSJ6/Br5CJdP7E/q2
-- Admin: admin123 -> $2b$12$8teOcWV6fy8jBArYWBnkAejU4iWs6fnd0eE6k3dCkI5ezhqTxxGwi
-- Test: test123 -> $2b$12$XEuYmYZtnGomlcnPEk7RWuc4ad0c1AcxIP9HmbYLPQ5UKCY/JFmEq
-- Bob/Carol: password123 -> $2a$10$8.H0Q1J5WCZD7kF6HCXXpO0OTgLt8kGU5ZJ5rSJ6/Br5CJdP7E/q2
INSERT INTO users (id, name, phone, email, password, address, role, is_admin, account_status, credit_limit) VALUES
('1', 'Alice', '+155501001', 'alice@secchamp.com', '$2a$10$8.H0Q1J5WCZD7kF6HCXXpO0OTgLt8kGU5ZJ5rSJ6/Br5CJdP7E/q2', '123 Main St, Springfield', 'USER', FALSE, 'ACTIVE', 500.00),
('2', 'Admin User', '+155501002', 'admin@secchamp.com', '$2b$12$8teOcWV6fy8jBArYWBnkAejU4iWs6fnd0eE6k3dCkI5ezhqTxxGwi', '456 Admin Ave, Springfield', 'ADMIN', TRUE, 'ACTIVE', 10000.00),
('3', 'Test Manager', '+155501003', 'test@secchamp.com', '$2b$12$XEuYmYZtnGomlcnPEk7RWuc4ad0c1AcxIP9HmbYLPQ5UKCY/JFmEq', '789 Test Blvd, Springfield', 'MANAGER', FALSE, 'ACTIVE', 2000.00),
('4', 'Bob Smith', '+155501004', 'bob@secbooks.com', '$2a$10$8.H0Q1J5WCZD7kF6HCXXpO0OTgLt8kGU5ZJ5rSJ6/Br5CJdP7E/q2', '321 Customer St, Springfield', 'USER', FALSE, 'ACTIVE', 100.00),
('5', 'Carol Johnson', '+155501005', 'carol@secbooks.com', '$2a$10$8.H0Q1J5WCZD7kF6HCXXpO0OTgLt8kGU5ZJ5rSJ6/Br5CJdP7E/q2', '654 Reader Rd, Springfield', 'USER', FALSE, 'SUSPENDED', 250.00);

-- Insert sample books
INSERT INTO books (id, title, author, isbn, category, price, description, stock, rating, image_url, publisher, publish_date) VALUES
('1', 'Clean Architecture', 'Robert C. Martin', '978-0134494166', 'Technology', 39.99, 'A comprehensive guide to building maintainable software systems with clean architecture principles.', 25, 4.5, '/images/clean-architecture.jpg', 'Prentice Hall', '2017-09-12'),
('2', 'The Phoenix Project', 'Gene Kim', '978-1942788294', 'Technology', 34.99, 'A novel about IT, DevOps, and helping your business win in the digital age.', 15, 4.7, '/images/phoenix-project.jpg', 'IT Revolution Press', '2018-01-10'),
('3', 'Cybersecurity Fundamentals', 'Charles J. Brooks', '978-1119362388', 'Security', 49.99, 'Essential concepts and practices for securing digital infrastructure and data.', 30, 4.2, '/images/cybersecurity-fundamentals.jpg', 'Wiley', '2019-03-15'),
('4', 'Spring Boot in Action', 'Craig Walls', '978-1617292545', 'Technology', 44.99, 'Complete guide to building applications with Spring Boot framework.', 20, 4.6, '/images/spring-boot-action.jpg', 'Manning Publications', '2020-05-20'),
('5', 'Web Application Security', 'Andrew Hoffman', '978-1492053118', 'Security', 54.99, 'Modern techniques for securing web applications against common vulnerabilities.', 18, 4.4, '/images/web-app-security.jpg', "O'Reilly Media", '2021-02-28'),
('6', 'Database Design for Mere Mortals', 'Michael J. Hernandez', '978-0321884497', 'Database', 42.99, 'Practical approach to relational database design and normalization.', 22, 4.3, '/images/database-design.jpg', 'Addison-Wesley', '2018-11-05'),
('7', 'JavaScript: The Good Parts', 'Douglas Crockford', '978-0596517748', 'Programming', 29.99, 'Distills JavaScript down to its elegant and useful core features.', 35, 4.1, '/images/js-good-parts.jpg', "O'Reilly Media", '2016-08-12'),
('8', 'System Design Interview', 'Alex Xu', '978-1736049112', 'Technology', 38.99, 'An insiders guide to system design interviews at tech companies.', 12, 4.8, '/images/system-design.jpg', 'Independently Published', '2022-01-15'),
('9', 'The Art of SQL', 'Stephane Faroult', '978-0596008949', 'Database', 36.99, 'Advanced SQL techniques for database professionals and developers.', 28, 4.0, '/images/art-of-sql.jpg', "O'Reilly Media", '2017-06-18'),
('10', 'Secure Coding Practices', 'Mark Dowd', '978-0321424778', 'Security', 59.99, 'Comprehensive guide to writing secure code and preventing vulnerabilities.', 14, 4.5, '/images/secure-coding.jpg', 'Addison-Wesley', '2020-10-22'),
('11', 'Microservices Patterns', 'Chris Richardson', '978-1617294549', 'Technology', 47.99, 'Design patterns for building reliable distributed systems with microservices.', 16, 4.4, '/images/microservices-patterns.jpg', 'Manning Publications', '2021-07-08'),
('12', 'Learning React', 'Alex Banks', '978-1492051718', 'Programming', 33.99, 'Modern patterns for developing React applications with hooks and context.', 26, 4.2, '/images/learning-react.jpg', "O'Reilly Media", '2021-09-14');

-- Create sample orders table (optional for future features)
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),
    book_id VARCHAR(50),
    quantity INT DEFAULT 1,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    total_amount DECIMAL(10,2),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Insert sample orders
INSERT INTO orders (id, user_id, book_id, quantity, status, total_amount) VALUES
('1', '1', '1', 1, 'COMPLETED', 39.99),
('2', '1', '3', 2, 'COMPLETED', 99.98),
('3', '4', '7', 1, 'PENDING', 29.99),
('4', '5', '2', 1, 'CANCELLED', 34.99);

-- Create purchase sessions table for temporary stock locks
CREATE TABLE IF NOT EXISTS purchase_sessions (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NULL,
    cart_id VARCHAR(50) NULL,
    locked_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    status VARCHAR(20) DEFAULT 'LOCKED',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Create shopping carts table
CREATE TABLE IF NOT EXISTS shopping_carts (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create cart items table
CREATE TABLE IF NOT EXISTS cart_items (
    id VARCHAR(50) PRIMARY KEY,
    cart_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    quantity INT DEFAULT 1,
    price_at_add DECIMAL(10,2),
    added_at TIMESTAMP NULL,
    reserved_until TIMESTAMP NULL,
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id),
    UNIQUE KEY unique_cart_book (cart_id, book_id)
);

-- Create user_books table for books owned by users (NO UNIQUE CONSTRAINT)
CREATE TABLE IF NOT EXISTS user_books (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    purchase_price DECIMAL(10,2),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
    -- Note: No unique constraint to allow multiple copies of the same book
);

-- Insert sample user book ownership (including multiple copies to test)
INSERT INTO user_books (id, user_id, book_id, purchase_price) VALUES
('1', '1', '1', 39.99),
('2', '1', '3', 49.99),
('3', '4', '7', 29.99),
('4', '1', '1', 39.99),  -- Alice owns 2 copies of Clean Architecture
('5', '1', '1', 39.99);  -- Alice owns 3 copies of Clean Architecture

COMMIT;
