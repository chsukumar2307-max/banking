package com.bank.controller;
import java.util.List;
import com.bank.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.bank.service.AccountService;
import com.bank.dto.request.DepositRequest;
import com.bank.dto.request.TransferRequest;
import com.bank.dto.request.WithdrawRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;

@SuppressWarnings("unused") // All methods are used as REST endpoints, so suppress IDE warnings for 'never used' methods
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
	private final AccountService accountService;

    private static final String INVALID_ACCOUNT_MSG = "Account number is required.";
    private static final String INVALID_AMOUNT_MSG = "Amount must be positive.";
    private static final String INVALID_FILTER_MSG = "Invalid filter format. Use key=value.";
    private static final String INTERNAL_SERVER_ERROR_MSG = "Internal server error";
    private static final String ACCOUNT_NOT_FOUND_MSG = "Account not found";

    @PostMapping(value = "/deposit", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {
        if (request == null || isBlank(request.getAccountNumber())) {
            log.warn("Deposit failed: account number is blank");
            return ResponseEntity.badRequest().body(new DepositResponse(INVALID_ACCOUNT_MSG, null));
        }
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            log.warn("Deposit failed: invalid amount for account {}", request.getAccountNumber());
            return ResponseEntity.badRequest().body(new DepositResponse(INVALID_AMOUNT_MSG, null));
        }
        try {
            DepositResponse response = accountService.deposit(request.getAccountNumber(), request.getAmount());
            log.info("Deposit successful for account {}", request.getAccountNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Deposit validation error for account {}: {}", request.getAccountNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(new DepositResponse(e.getMessage()));
        } catch (AccountNotFoundException e) {
            log.warn("Deposit account not found: {}", request.getAccountNumber());
            return ResponseEntity.status(404).body(new DepositResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Deposit error for account {}: {}", request.getAccountNumber(), e.getMessage());
            return ResponseEntity.internalServerError().body(new DepositResponse(INTERNAL_SERVER_ERROR_MSG));
        }
    }

    @PostMapping(value = "/withdraw", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WithdrawResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        if (request == null || isBlank(request.getAccountNumber())) {
            log.warn("Withdraw failed: account number is blank");
            return ResponseEntity.badRequest().body(new WithdrawResponse(INVALID_ACCOUNT_MSG, null));
        }
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            log.warn("Withdraw failed: invalid amount for account {}", request.getAccountNumber());
            return ResponseEntity.badRequest().body(new WithdrawResponse(INVALID_AMOUNT_MSG, null));
        }
        try {
            WithdrawResponse response = accountService.withdraw(request.getAccountNumber(), request.getAmount());
            log.info("Withdraw successful for account {}", request.getAccountNumber());
            return ResponseEntity.ok(response);
        } catch (InsufficientBalanceException e) {
            log.warn("Withdraw insufficient balance for account {}: {}", request.getAccountNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(new WithdrawResponse("Insufficient balance"));
        } catch (IllegalArgumentException e) {
            log.warn("Withdraw validation error for account {}: {}", request.getAccountNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(new WithdrawResponse(e.getMessage(), null));
        } catch (AccountNotFoundException e) {
            log.warn("Withdraw account not found: {}", request.getAccountNumber());
            return ResponseEntity.status(404).body(new WithdrawResponse(ACCOUNT_NOT_FOUND_MSG));
        } catch (Exception e) {
            log.error("Withdraw error for account {}: {}", request.getAccountNumber(), e.getMessage());
            return ResponseEntity.internalServerError().body(new WithdrawResponse(INTERNAL_SERVER_ERROR_MSG));
        }
    }

    @PostMapping(value = "/transfer", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transfer(@Valid @RequestBody TransferRequest request) {
        if (request == null || isBlank(request.getFromAccount()) || isBlank(request.getToAccount())) {
            log.warn("Transfer failed: from/to account is blank");
            return ResponseEntity.badRequest().body(new MessageResponse(INVALID_ACCOUNT_MSG));
        }
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            log.warn("Transfer failed: invalid amount from {} to {}", request.getFromAccount(), request.getToAccount());
            return ResponseEntity.badRequest().body(new MessageResponse(INVALID_AMOUNT_MSG));
        }
        try {
            Object response = accountService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount());
            log.info("Transfer successful from {} to {}", request.getFromAccount(), request.getToAccount());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Transfer validation error from {} to {}: {}", request.getFromAccount(), request.getToAccount(), e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (InsufficientBalanceException e) {
            log.warn("Transfer insufficient balance from {} to {}: {}", request.getFromAccount(), request.getToAccount(), e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (AccountNotFoundException e) {
            log.warn("Transfer account not found: from {} to {}", request.getFromAccount(), request.getToAccount());
            return ResponseEntity.status(404).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Transfer error from {} to {}: {}", request.getFromAccount(), request.getToAccount(), e.getMessage());
            return ResponseEntity.internalServerError().body(new MessageResponse(INTERNAL_SERVER_ERROR_MSG));
        }
    }

    @GetMapping(value = "/balance/{accountNumber}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> balance(@PathVariable String accountNumber) {
        if (isBlank(accountNumber)) {
            log.warn("Balance check failed: account number is blank");
            return ResponseEntity.badRequest().body(new MessageResponse(INVALID_ACCOUNT_MSG));
        }
        try {
            BalanceResponse response = accountService.balance(accountNumber);
            log.info("Balance check successful for account {}", accountNumber);
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException e) {
            log.warn("Balance account not found: {}", accountNumber);
            return ResponseEntity.status(404).body(new MessageResponse(ACCOUNT_NOT_FOUND_MSG));
        } catch (Exception e) {
            log.error("Balance check error for account {}: {}", accountNumber, e.getMessage());
            return ResponseEntity.internalServerError().body(new MessageResponse(INTERNAL_SERVER_ERROR_MSG));
        }
    }

    @GetMapping(value = "/transactions/{accountNumber}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transactions(@PathVariable String accountNumber) {
        if (isBlank(accountNumber)) {
            log.warn("Transactions fetch failed: account number is blank");
            return ResponseEntity.badRequest().body(new MessageResponse(INVALID_ACCOUNT_MSG));
        }
        try {
            List<TransactionResponse> response = accountService.transactions(accountNumber);
            log.info("Transactions fetch successful for account {}", accountNumber);
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException e) {
            log.warn("Transactions account not found: {}", accountNumber);
            return ResponseEntity.status(404).body(new MessageResponse(ACCOUNT_NOT_FOUND_MSG));
        } catch (Exception e) {
            log.error("Transactions fetch error for account {}: {}", accountNumber, e.getMessage());
            return ResponseEntity.internalServerError().body(new MessageResponse(INTERNAL_SERVER_ERROR_MSG));
        }
    }

    @GetMapping(value = "/transactionsHistory/{filter}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transactionsHistoryByFilter(@PathVariable String filter) {
        if (isBlank(filter)) {
            log.warn("Transaction history filter is blank");
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(List.of());
        }
        String[] parts = filter.split("=", 2);
        if (parts.length != 2) {
            log.warn("Transaction history filter format invalid: {}", filter);
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(List.of());
        }
        String key = parts[0];
        String value = parts[1];
        List<TransactionResponse> results;
        try {
            switch (key) {
                case "transactionId":
                    results = handleTransactionIdFilter(value);
                    if (results == null) {
                        // If invalid number, treat as bad request (test expects 400)
                        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(List.of());
                    }
                    break;
                case "accountNumber":
                    results = accountService.searchTransactions(null, value, null, null, 0, 10);
                    break;
                case "transactionType":
                    results = accountService.searchTransactions(null, null, null, value, 0, 10);
                    break;
                default:
                    log.warn("Unknown filter key: {}", key);
                    return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(List.of());
            }
            log.info("Transaction history fetch by filter {}={} successful", key, value);
            return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(results != null ? results : List.of());
        } catch (Exception e) {
            log.error("Transaction history fetch error for filter {}={}: {}", key, value, e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse(INTERNAL_SERVER_ERROR_MSG));
        }
    }

    // Extracted helper to reduce cognitive complexity/code smell
    private List<TransactionResponse> handleTransactionIdFilter(String value) {
        try {
            Long id = Long.parseLong(value);
            List<TransactionResponse> result = accountService.searchTransactions(id, null, null, null, 0, 10);
            return result != null ? result : List.of(); // Return empty collection instead of null
        } catch (NumberFormatException e) {
            log.warn("Invalid transactionId in filter: {}", value);
            return null; // Still return null to indicate invalid number for controller logic
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
