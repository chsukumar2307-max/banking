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

@SuppressWarnings("unused") // All methods are used as REST endpoints, so suppress IDE warnings for 'never used' methods
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@PostMapping(value = "/deposit", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {
		return ResponseEntity.ok(accountService.deposit(request.getAccountNumber(), request.getAmount()));
	}

	@PostMapping(value = "/withdraw", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WithdrawResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
		return ResponseEntity.ok(accountService.withdraw(request.getAccountNumber(), request.getAmount()));
	}

	@PostMapping(value = "/transfer", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
		return ResponseEntity.ok(accountService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount()));
	}

	@GetMapping(value = "/balance/{accountNumber}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BalanceResponse> balance(@PathVariable String accountNumber) {
		return ResponseEntity.ok(accountService.balance(accountNumber));
	}

	@GetMapping(value = "/transactions/{accountNumber}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TransactionResponse>> transactions(@PathVariable String accountNumber) {
		return ResponseEntity.ok(accountService.transactions(accountNumber));
	}

	@GetMapping(value = "/transactionsHistory/{filter}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TransactionResponse>> transactionsHistoryByFilter(@PathVariable String filter) {
		String[] parts = filter.split("=", 2);
		if (parts.length != 2) {
			return ResponseEntity.badRequest().body(List.of());
		}
		String key = parts[0];
		String value = parts[1];
		List<TransactionResponse> results;
		switch (key) {
			case "transactionId":
				try {
					Long id = Long.parseLong(value);
					results = accountService.searchTransactions(id, null, null, null, 0, 10);
				} catch (NumberFormatException e) {
					return ResponseEntity.badRequest().body(List.of());
				}
				break;
			case "accountNumber":
				results = accountService.searchTransactions(null, value, null, null, 0, 10);
				break;
			case "transactionType":
				results = accountService.searchTransactions(null, null, null, value, 0, 10);
				break;
			default:
				return ResponseEntity.badRequest().body(List.of());
		}
		return ResponseEntity.ok(results);
	}
}
