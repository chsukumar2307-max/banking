package com.bank.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {
    private String status;
    private BigDecimal balance;
    private String message;

    public DepositResponse(String status, BigDecimal balance) {
        this.status = status;
        this.balance = balance;
    }

    public DepositResponse(String message) {
        this.message = message;
    }
}
