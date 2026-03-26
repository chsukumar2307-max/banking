# Feature Spec: add-transaction-statement

## Description
Add a 'statement' field to the transaction table and Transaction entity. The statement should record the source and destination accounts for transfer transactions, e.g., 'fromAccount: ACC828969, toAccount: ACC324188'. Update the transfer method in AccountServiceImpl to populate this field only for transfer transactions. Ensure the field is included in relevant DTOs and database migration scripts.

## Created
2026-03-19

## Actors
- System (backend)
- Account holder (initiator of transfer)
- Admin/user viewing transaction history

## Actions
- Add 'statement' field to transaction table/entity
- Populate field for transfer transactions
- Update transfer method
- Include field in DTOs
- Update migration scripts

## Data
- Transaction table/entity
- Statement field (string, format: 'fromAccount: ACCxxxx, toAccount: ACCyyyy')
- Source/destination account numbers

## Constraints
- Statement field only populated for transfer transactions
- Field must be included in DTOs and migration scripts

## Success Criteria
- All transfer transactions have a correctly formatted statement field
- Non-transfer transactions have a null or empty statement field
- Statement field is visible in transaction DTOs and API responses
- Migration script successfully adds the field without data loss

## Assumptions
- Statement field is nullable for non-transfer transactions
- Format is fixed as 'fromAccount: <source>, toAccount: <destination>'
- All transaction DTOs include the statement field

