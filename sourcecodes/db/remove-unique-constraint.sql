-- Migration script to remove unique constraint on user_books table
-- This allows users to purchase multiple copies of the same book

USE secchamp2025;

-- Remove the unique constraint that prevents multiple copies
ALTER TABLE user_books DROP INDEX unique_user_book;

-- Add some test data to verify multiple copies work
-- (These will be cleaned up by the application, just for testing)
INSERT INTO user_books (id, user_id, book_id, purchase_price) VALUES
('test1', '1', '1', 39.99),  -- Alice buying another copy of Clean Architecture
('test2', '1', '1', 39.99),  -- Alice buying yet another copy of Clean Architecture
('test3', '4', '7', 29.99);  -- Bob buying another copy of JavaScript: The Good Parts

-- Verify the change worked
SELECT user_id, book_id, COUNT(*) as copies_owned 
FROM user_books 
GROUP BY user_id, book_id 
HAVING COUNT(*) > 1;

COMMIT;
