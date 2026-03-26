# Comprehensive Test Cases for Spring Boot Banking Application

## 1. Functional Test Cases

### Deposit
- **Test case name:** Deposit Success
  - **API endpoint:** POST /api/accounts/deposit
  - **Request:** {"accountNumber": "12345", "amount": 1000.0}
  - **Expected response:** 200 OK, {"balance": 5000.0}
  - **Test type:** Positive
  - **Recommended:** Unit, Integration

- **Test case name:** Deposit Invalid Amount
  - **API endpoint:** POST /api/accounts/deposit
  - **Request:** {"accountNumber": "12345", "amount": -100.0}
  - **Expected response:** 400 Bad Request
  - **Test type:** Negative, Validation
  - **Recommended:** Unit

### Withdraw
- **Test case name:** Withdraw Success
  - **API endpoint:** POST /api/accounts/withdraw
  - **Request:** {"accountNumber": "12345", "amount": 500.0}
  - **Expected response:** 200 OK, {"balance": 4500.0}
  - **Test type:** Positive
  - **Recommended:** Unit, Integration

- **Test case name:** Withdraw Insufficient Balance
  - **API endpoint:** POST /api/accounts/withdraw
  - **Request:** {"accountNumber": "12345", "amount": 10000.0}
  - **Expected response:** 500 Internal Server Error, {"error": "Insufficient balance"}
  - **Test type:** Negative, Error Handling
  - **Recommended:** Unit

### Transfer
- **Test case name:** Transfer Success
  - **API endpoint:** POST /api/accounts/transfer
  - **Request:** {"fromAccount": "12345", "toAccount": "67890", "amount": 500.0}
  - **Expected response:** 200 OK, {"status": "Success"}
  - **Test type:** Positive
  - **Recommended:** Unit, Integration

- **Test case name:** Transfer Same Account
  - **API endpoint:** POST /api/accounts/transfer
  - **Request:** {"fromAccount": "12345", "toAccount": "12345", "amount": 500.0}
  - **Expected response:** 500 Internal Server Error, {"error": "fromAccount and toAccount must be different"}
  - **Test type:** Negative, Validation
  - **Recommended:** Unit

### Balance
- **Test case name:** Get Balance Success
  - **API endpoint:** GET /api/accounts/balance/12345
  - **Request:** N/A
  - **Expected response:** 200 OK, {"balance": 5000.0}
  - **Test type:** Positive
  - **Recommended:** Unit, Integration

- **Test case name:** Get Balance Invalid Account
  - **API endpoint:** GET /api/accounts/balance/99999
  - **Request:** N/A
  - **Expected response:** 500 Internal Server Error, {"error": "Account not found"}
  - **Test type:** Negative, Error Handling
  - **Recommended:** Unit

### Transaction History
- **Test case name:** Get Transactions Success
  - **API endpoint:** GET /api/accounts/transactions/12345
  - **Request:** N/A
  - **Expected response:** 200 OK, [Transaction List]
  - **Test type:** Positive
  - **Recommended:** Unit, Integration

- **Test case name:** Get Transactions Empty
  - **API endpoint:** GET /api/accounts/transactions/12345
  - **Request:** N/A
  - **Expected response:** 200 OK, []
  - **Test type:** Edge
  - **Recommended:** Unit

## 2. Validation Test Cases (@Valid scenarios)
- **Test case name:** Deposit Missing Account Number
  - **API endpoint:** POST /api/accounts/deposit
  - **Request:** {"amount": 1000.0}
  - **Expected response:** 400 Bad Request
  - **Test type:** Validation
  - **Recommended:** Unit

- **Test case name:** Withdraw Invalid Amount Format
  - **API endpoint:** POST /api/accounts/withdraw
  - **Request:** {"accountNumber": "12345", "amount": "abc"}
  - **Expected response:** 400 Bad Request
  - **Test type:** Validation
  - **Recommended:** Unit

## 3. Security Test Cases (JWT, unauthorized, expired, blacklisted tokens)
- **Test case name:** Access Protected Endpoint Without JWT
  - **API endpoint:** GET /api/accounts/balance/12345
  - **Request:** N/A
  - **Expected response:** 401 Unauthorized
  - **Test type:** Security
  - **Recommended:** Integration

- **Test case name:** Access With Expired JWT
  - **API endpoint:** GET /api/accounts/balance/12345
  - **Request:** Authorization: Bearer <expired_token>
  - **Expected response:** 401 Unauthorized
  - **Test type:** Security
  - **Recommended:** Integration

- **Test case name:** Access With Blacklisted JWT
  - **API endpoint:** GET /api/accounts/balance/12345
  - **Request:** Authorization: Bearer <blacklisted_token>
  - **Expected response:** 401 Unauthorized
  - **Test type:** Security
  - **Recommended:** Integration

- **Test case name:** Logout Success (Token Blacklist)
  - **API endpoint:** POST /api/auth/logout
  - **Request:** Authorization: Bearer <valid_token>
  - **Expected response:** 200 OK, {"message": "Logged out"}
  - **Test type:** Security
  - **Recommended:** Integration

## 4. Edge Cases (concurrency, large transactions)
- **Test case name:** Concurrent Withdrawals
  - **API endpoint:** POST /api/accounts/withdraw
  - **Request:** Multiple simultaneous requests for withdrawal from same account
  - **Expected response:** Only one succeeds, others fail with "Insufficient balance"
  - **Test type:** Edge
  - **Recommended:** Performance, Integration

- **Test case name:** Large Deposit Transaction
  - **API endpoint:** POST /api/accounts/deposit
  - **Request:** {"accountNumber": "12345", "amount": 10000000.0}
  - **Expected response:** 200 OK, {"balance": <updated_balance>}
  - **Test type:** Edge
  - **Recommended:** Performance

## 5. Integration Test Scenarios (end-to-end flow)
- **Test case name:** End-to-End Transfer Flow
  - **API endpoint:** POST /api/accounts/transfer
  - **Request:** {"fromAccount": "12345", "toAccount": "67890", "amount": 500.0}
  - **Expected response:** 200 OK, {"status": "Success"}
  - **Test type:** Integration
  - **Recommended:** Integration

- **Test case name:** End-to-End Deposit and Withdraw Flow
  - **API endpoint:** POST /api/accounts/deposit, POST /api/accounts/withdraw
  - **Request:** Deposit then withdraw
  - **Expected response:** Correct balances after each operation
  - **Test type:** Integration
  - **Recommended:** Integration

## 6. Error Handling Cases
- **Test case name:** Deposit to Non-Existent Account
  - **API endpoint:** POST /api/accounts/deposit
  - **Request:** {"accountNumber": "99999", "amount": 1000.0}
  - **Expected response:** 500 Internal Server Error, {"error": "Account not found"}
  - **Test type:** Error Handling
  - **Recommended:** Unit, Integration

- **Test case name:** Withdraw with Invalid Account
  - **API endpoint:** POST /api/accounts/withdraw
  - **Request:** {"accountNumber": "99999", "amount": 100.0}
  - **Expected response:** 500 Internal Server Error, {"error": "Account not found"}
  - **Test type:** Error Handling
  - **Recommended:** Unit, Integration

## 7. Audit Logging
- **Test case name:** Audit Log Created for Deposit
  - **API endpoint:** POST /api/accounts/deposit
  - **Request:** {"accountNumber": "12345", "amount": 1000.0}
  - **Expected response:** Audit log entry created for deposit action
  - **Test type:** Functional, Audit
  - **Recommended:** Integration

- **Test case name:** Audit Log Created for Transfer
  - **API endpoint:** POST /api/accounts/transfer
  - **Request:** {"fromAccount": "12345", "toAccount": "67890", "amount": 500.0}
  - **Expected response:** Audit log entry created for transfer action
  - **Test type:** Functional, Audit
  - **Recommended:** Integration

---

# Test Type Recommendations
- **Unit tests:** Input validation, business logic, error handling
- **Integration tests:** API flows, security, audit logging, end-to-end scenarios
- **Performance tests:** Concurrency, large transactions

# Coverage Note
All test cases are designed to be error-free and cover positive, negative, validation, security, edge, integration, and audit scenarios for your Spring Boot Banking Application.

