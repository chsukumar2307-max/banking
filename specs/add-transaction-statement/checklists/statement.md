# Checklist: add-transaction-statement

## Database Schema
- [ ] Add nullable 'statement' field to transaction table
- [ ] Create migration script to add 'statement' field without data loss
- [ ] Verify migration works for existing data

## Entity & Model
- [ ] Add 'statement' field to Transaction entity/class
- [ ] Ensure field is nullable for non-transfer transactions

## Service Logic
- [ ] Update AccountServiceImpl transfer method to populate 'statement' field with correct format
- [ ] Ensure 'statement' is only set for transfer transactions
- [ ] Validate correct statement format: 'fromAccount: <source>, toAccount: <destination>'

## DTOs & API
- [ ] Add 'statement' field to all relevant transaction DTOs
- [ ] Ensure API responses include 'statement' field for transfer transactions

## Testing
- [ ] Add/Update unit tests for transfer method to check 'statement' field
- [ ] Add/Update integration tests for transaction API responses
- [ ] Test migration script on sample data

## Documentation
- [ ] Update API documentation to include 'statement' field in transaction responses
- [ ] Document migration steps and rollback procedure

## Review
- [ ] Peer review for data integrity and migration safety
- [ ] Checklist used for both development and PR review

## Risk & Quality
- [ ] Confirm no data loss during migration
- [ ] Confirm audit logging (if applicable) includes 'statement' field
- [ ] Validate rollback plan for migration

