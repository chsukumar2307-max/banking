package com.bank.service.impl;
import com.bank.dto.response.*;
import com.bank.entity.Customer;
import com.bank.entity.Transaction;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Value("${app.account.default-page-size:10}")
    private int defaultPageSize;
    private static final int FALLBACK_PAGE_SIZE = 10;

    // Error messages
    private static final String ERR_WITHDRAW_AMOUNT = "Withdraw amount must be positive and account number must be provided";
    private static final String ERR_DEPOSIT_AMOUNT = "Deposit amount must be positive and non-null";
    private static final String ERR_TRANSFER_AMOUNT = "Transfer amount must be positive and account numbers must be provided";
    private static final String ERR_TRANSFER_SAME_ACCOUNT = "fromAccount and toAccount must be different";
    private static final String ERR_ACCOUNT_NOT_FOUND = "Account is not found: ";
    private static final String ERR_INSUFFICIENT_BALANCE = "Insufficient balance";

    @Override
    @Transactional
    public DepositResponse deposit(String accountNumber, BigDecimal amount) {
        log.info("Deposit request received for account {}", accountNumber);
        validateAccountAndAmount(accountNumber, amount, ERR_DEPOSIT_AMOUNT);
        Customer c = getCustomerForUpdate(accountNumber);
        if (c == null) {
            log.error("Deposit failed: account not found for {}", accountNumber);
            throw new AccountNotFoundException(ERR_ACCOUNT_NOT_FOUND + accountNumber);
        }
        BigDecimal newBalance = c.getBalance().add(amount);
        c.setBalance(newBalance);
        customerRepository.save(c);
        saveTransaction(accountNumber, TransactionType.DEPOSIT, amount, newBalance, TransactionStatus.SUCCESS, null);
        log.info("Deposit successful for account {}: new balance {}", accountNumber, newBalance);
        return new DepositResponse("Deposit successful", newBalance);
    }

    @Override
    @Transactional
    public WithdrawResponse withdraw(String accountNumber, BigDecimal amount) {
        log.info("Withdraw request received for account {}", accountNumber);
        validateAccountAndAmount(accountNumber, amount, ERR_WITHDRAW_AMOUNT);
        Customer c = getCustomerForUpdate(accountNumber);
        if (c == null) {
            log.error("Withdraw failed: account not found for {}", accountNumber);
            throw new AccountNotFoundException(ERR_ACCOUNT_NOT_FOUND + accountNumber);
        }
        if (c.getBalance().compareTo(amount) < 0) {
            saveTransaction(accountNumber, TransactionType.WITHDRAW, amount, c.getBalance(), TransactionStatus.FAILED, null);
            log.warn("Insufficient balance for withdrawal: accountNumber={}, requested={}, available={}", accountNumber, amount, c.getBalance());
            throw new InsufficientBalanceException(ERR_INSUFFICIENT_BALANCE);
        }
        BigDecimal newBalance = c.getBalance().subtract(amount);
        c.setBalance(newBalance);
        customerRepository.save(c);
        saveTransaction(accountNumber, TransactionType.WITHDRAW, amount, newBalance, TransactionStatus.SUCCESS, null);
        log.info("Withdrawal successful for account {}: new balance {}", accountNumber, newBalance);
        return new WithdrawResponse("Withdrawal successful", newBalance);
    }

    @Override
    @Transactional
    public TransferResponse transfer(String fromAccount, String toAccount, BigDecimal amount) {
        log.info("Transfer request received from {} to {}", fromAccount, toAccount);
        validateTransfer(fromAccount, toAccount, amount);
        List<Customer> customers = getOrderedCustomers(fromAccount, toAccount);
        if (customers.size() != 2) {
            log.error("Transfer failed: could not retrieve both accounts {} and {}", fromAccount, toAccount);
            throw new AccountNotFoundException(ERR_ACCOUNT_NOT_FOUND + fromAccount + ", " + toAccount);
        }
        Customer from = getFromCustomer(fromAccount, customers);
        Customer to = getToCustomer(toAccount, customers);
        if (from == null || to == null) {
            log.error("Transfer failed: null customer reference");
            throw new AccountNotFoundException("Null customer reference");
        }
        checkSufficientBalanceOrFail(from, fromAccount, amount);
        performTransfer(from, to, fromAccount, toAccount, amount);
        log.info("Transfer successful from {} to {}: amount {}", fromAccount, toAccount, amount);
        return new TransferResponse("Transfer successful");
    }

    private List<Customer> getOrderedCustomers(String fromAccount, String toAccount) {
        List<String> ordered = Stream.of(fromAccount, toAccount).sorted(Comparator.naturalOrder()).toList();
        // SonarQube: Use method reference for a single possibly-throwing invocation
        return ordered.stream().map(this::getCustomerForUpdate).toList();
    }

    private Customer getFromCustomer(String fromAccount, List<Customer> customers) {
        return fromAccount.equals(customers.get(0).getAccountNumber()) ? customers.get(0) : customers.get(1);
    }

    private Customer getToCustomer(String toAccount, List<Customer> customers) {
        return toAccount.equals(customers.get(0).getAccountNumber()) ? customers.get(0) : customers.get(1);
    }

    private void checkSufficientBalanceOrFail(Customer from, String fromAccount, BigDecimal amount) {
        if (from.getBalance().compareTo(amount) < 0) {
            saveTransaction(fromAccount, TransactionType.TRANSFER_OUT, amount, from.getBalance(), TransactionStatus.FAILED, null);
            log.warn("Insufficient balance for transfer: fromAccount={}, requested={}, available={}", fromAccount, amount, from.getBalance());
            throw new InsufficientBalanceException(ERR_INSUFFICIENT_BALANCE);
        }
    }

    private void performTransfer(Customer from, Customer to, String fromAccount, String toAccount, BigDecimal amount) {
        BigDecimal fromNew = from.getBalance().subtract(amount);
        BigDecimal toNew = to.getBalance().add(amount);
        from.setBalance(fromNew);
        to.setBalance(toNew);
        customerRepository.save(from);
        customerRepository.save(to);
        String statement = String.format("fromAccount: %s, toAccount: %s", fromAccount, toAccount);
        saveTransaction(fromAccount, TransactionType.TRANSFER_OUT, amount, fromNew, TransactionStatus.SUCCESS, statement);
        saveTransaction(toAccount, TransactionType.TRANSFER_IN, amount, toNew, TransactionStatus.SUCCESS, statement);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse balance(String accountNumber) {
        if (!StringUtils.hasText(accountNumber)) {
            log.warn("Balance check failed: account number is blank");
            throw new IllegalArgumentException("Account number must be provided");
        }
        Customer c = customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        return new BalanceResponse(accountNumber, c.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> transactions(String accountNumber) {
        if (!StringUtils.hasText(accountNumber)) {
            log.warn("Transaction history failed: account number is blank");
            throw new IllegalArgumentException("Account number must be provided");
        }
        if (customerRepository.findByAccountNumber(accountNumber).isEmpty()) {
            log.warn("Transaction history request for non-existent account: {}", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return transactionRepository.findByAccountNumberOrderByTransactionDateDesc(accountNumber)
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> searchTransactions(Long transactionId, String accountNumber, String transactionDate, String transactionType, Integer page, Integer size) {
        Specification<Transaction> spec = buildTransactionSpecification(transactionId, accountNumber, transactionDate, transactionType);
        int pageNum = page != null && page > 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : defaultPageSize;
        if (pageSize <= 0) {
            log.warn("Invalid page size: {}. Falling back to defaultPageSize={}", pageSize, FALLBACK_PAGE_SIZE);
            pageSize = FALLBACK_PAGE_SIZE;
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "transactionDate"));
        return transactionRepository.findAll(spec, pageable)
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    private Specification<Transaction> buildTransactionSpecification(Long transactionId, String accountNumber, String transactionDate, String transactionType) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            addTransactionIdPredicate(predicates, root, cb, transactionId);
            addAccountNumberPredicate(predicates, root, cb, accountNumber);
            addTransactionDatePredicate(predicates, root, cb, transactionDate);
            addTransactionTypePredicate(predicates, root, cb, transactionType);
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private void addTransactionIdPredicate(java.util.List<jakarta.persistence.criteria.Predicate> predicates, jakarta.persistence.criteria.Root<Transaction> root, jakarta.persistence.criteria.CriteriaBuilder cb, Long transactionId) {
        if (transactionId != null) {
            predicates.add(cb.equal(root.get("transactionId"), transactionId));
        }
    }

    private void addAccountNumberPredicate(java.util.List<jakarta.persistence.criteria.Predicate> predicates, jakarta.persistence.criteria.Root<Transaction> root, jakarta.persistence.criteria.CriteriaBuilder cb, String accountNumber) {
        if (StringUtils.hasText(accountNumber)) {
            predicates.add(cb.equal(root.get("accountNumber"), accountNumber));
        }
    }

    private void addTransactionDatePredicate(java.util.List<jakarta.persistence.criteria.Predicate> predicates, jakarta.persistence.criteria.Root<Transaction> root, jakarta.persistence.criteria.CriteriaBuilder cb, String transactionDate) {
        if (StringUtils.hasText(transactionDate)) {
            java.time.Instant date = parseTransactionDate(transactionDate);
            if (date != null) {
                predicates.add(cb.equal(root.get("transactionDate"), date));
            }
        }
    }

    private java.time.Instant parseTransactionDate(String transactionDate) {
        try {
            return java.time.Instant.parse(transactionDate);
        } catch (Exception e) {
            log.warn("Invalid transactionDate format: {}", transactionDate);
            return null;
        }
    }

    private void addTransactionTypePredicate(java.util.List<jakarta.persistence.criteria.Predicate> predicates, jakarta.persistence.criteria.Root<Transaction> root, jakarta.persistence.criteria.CriteriaBuilder cb, String transactionType) {
        if (StringUtils.hasText(transactionType)) {
            TransactionType type = parseTransactionType(transactionType);
            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }
        }
    }

    private TransactionType parseTransactionType(String transactionType) {
        try {
            return TransactionType.valueOf(transactionType);
        } catch (Exception e) {
            log.warn("Invalid transactionType: {}", transactionType);
            return null;
        }
    }

    // --- Private helpers for code smell reduction ---

    private void validateAccountAndAmount(String accountNumber, BigDecimal amount, String errorMessage) {
        if (!StringUtils.hasText(accountNumber) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateTransfer(String fromAccount, String toAccount, BigDecimal amount) {
        if (!StringUtils.hasText(fromAccount) || !StringUtils.hasText(toAccount) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(ERR_TRANSFER_AMOUNT);
            throw new IllegalArgumentException(ERR_TRANSFER_AMOUNT);
        }
        if (fromAccount.equals(toAccount)) {
            log.warn(ERR_TRANSFER_SAME_ACCOUNT);
            throw new IllegalArgumentException(ERR_TRANSFER_SAME_ACCOUNT);
        }
    }

    private Customer getCustomerForUpdate(String accountNumber) {
        return customerRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(ERR_ACCOUNT_NOT_FOUND + accountNumber));
    }

    private void saveTransaction(String accountNumber, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, TransactionStatus status, String statement) {
        transactionRepository.save(Transaction.builder()
                .accountNumber(accountNumber)
                .transactionType(type)
                .amount(amount)
                .balanceAfterTransaction(balanceAfter)
                .status(status)
                .statement(statement)
                .build());
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return new TransactionResponse(
                t.getTransactionId(),
                t.getAccountNumber(),
                t.getTransactionType(),
                t.getAmount(),
                t.getBalanceAfterTransaction(),
                t.getTransactionDate(),
                t.getStatus(),
                t.getStatement()
        );
    }
}
