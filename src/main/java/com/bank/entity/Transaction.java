package com.bank.entity;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@SuppressWarnings("unused")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions", indexes = {
		@Index(name = "idx_tx_account_date", columnList = "account_number,transaction_date")
})
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "account_number", nullable = false, length = 20)
	private String accountNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false, length = 30)
	private TransactionType transactionType;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "balance_after_transaction", nullable = false, precision = 19, scale = 2)
	private BigDecimal balanceAfterTransaction;

	@Column(name = "transaction_date", nullable = false, updatable = false)
	private Instant transactionDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TransactionStatus status;

	@Column(name = "statement")
	private String statement;

	@PrePersist
	void onCreate() {
		if (transactionDate == null) transactionDate = Instant.now();
		if (status == null) status = TransactionStatus.SUCCESS;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Transaction that = (Transaction) o;
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
		return "Transaction{" +
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
