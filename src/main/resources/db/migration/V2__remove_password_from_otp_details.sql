-- Remove password column from otp_details table
SET FOREIGN_KEY_CHECKS=0;
ALTER TABLE otp_details DROP COLUMN password;
SET FOREIGN_KEY_CHECKS=1;

