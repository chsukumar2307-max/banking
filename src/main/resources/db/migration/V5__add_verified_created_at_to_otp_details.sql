-- Add verified_at and created_at columns to otp_details table
ALTER TABLE otp_details ADD COLUMN verified_at TIMESTAMP NULL;
ALTER TABLE otp_details ADD COLUMN created_at TIMESTAMP NULL;

