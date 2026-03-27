package com.bank.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponse {
	private String status;
	private BigDecimal balance;
	private String message;

	public WithdrawResponse(String status, BigDecimal balance) {
		this.status = status;
		this.balance = balance;
	}

	public WithdrawResponse(String message) {
		this.message = message;
	}
}
