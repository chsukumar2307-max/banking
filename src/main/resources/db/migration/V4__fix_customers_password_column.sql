-- Ensure password column in customers table can hold BCrypt hashes (60+ characters)
ALTER TABLE customers MODIFY COLUMN password VARCHAR(255) NOT NULL;
