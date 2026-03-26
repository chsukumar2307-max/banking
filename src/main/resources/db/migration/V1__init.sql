CREATE TABLE customers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  mobile_number VARCHAR(15) NOT NULL,
  password VARCHAR(255) NOT NULL,
  account_number VARCHAR(20) NOT NULL,
  balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_customers_username UNIQUE (username),
  CONSTRAINT uk_customers_mobile UNIQUE (mobile_number),
  CONSTRAINT uk_customers_account UNIQUE (account_number)
);

CREATE TABLE otp_details (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  mobile_number VARCHAR(15) NOT NULL,
  otp VARCHAR(10) NOT NULL,
  expiry_time TIMESTAMP NOT NULL,
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uk_otp_username_mobile UNIQUE (username, mobile_number)
);

CREATE TABLE transactions (
  transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_number VARCHAR(20) NOT NULL,
  transaction_type VARCHAR(30) NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  balance_after_transaction DECIMAL(19,2) NOT NULL,
  transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) NOT NULL,
  INDEX idx_tx_account_date (account_number, transaction_date)
);

