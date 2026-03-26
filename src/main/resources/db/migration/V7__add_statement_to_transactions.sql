-- Migration: Add statement column to transactions table
ALTER TABLE transactions ADD COLUMN statement VARCHAR(255) NULL;
