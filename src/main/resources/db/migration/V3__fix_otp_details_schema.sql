-- Ensure password column is removed from otp_details table
-- First, modify the column to allow NULL and have a default value
ALTER TABLE otp_details MODIFY COLUMN password VARCHAR(255) NULL DEFAULT NULL;

-- Then drop the column
ALTER TABLE otp_details DROP COLUMN password;



