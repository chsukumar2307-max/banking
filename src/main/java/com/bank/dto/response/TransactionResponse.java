package com.bank.dto.response;
import com.bank.entity.TransactionStatus;
import com.bank.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long transactionId;
    private String accountNumber;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private Instant transactionDate;
    private TransactionStatus status;
    private String statement;

    // Add equals, hashCode, toString for testability and coverage
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionResponse that = (TransactionResponse) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(accountNumber, that.accountNumber) &&
                transactionType == that.transactionType &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(balanceAfterTransaction, that.balanceAfterTransaction) &&
                Objects.equals(transactionDate, that.transactionDate) &&
                status == that.status &&
                Objects.equals(statement, that.statement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, accountNumber, transactionType, amount, balanceAfterTransaction, transactionDate, status, statement);
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "transactionId=" + transactionId +
                ", accountNumber='" + accountNumber + '\'' +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", balanceAfterTransaction=" + balanceAfterTransaction +
                ", transactionDate=" + transactionDate +
                ", status=" + status +
                ", statement='" + statement + '\'' +
                '}';
    }
}
