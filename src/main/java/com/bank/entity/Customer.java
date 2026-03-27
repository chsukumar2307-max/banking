package com.bank.entity;
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
@Table(name = "customers",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_customers_username", columnNames = "username"),
				@UniqueConstraint(name = "uk_customers_mobile", columnNames = "mobile_number"),
				@UniqueConstraint(name = "uk_customers_account", columnNames = "account_number")
		})
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String username;

	@Column(unique = true,  name = "mobile_number", nullable = false, length = 15)
	private String mobileNumber;

	@Column(nullable = false, length = 225)
	private String password;

	@Column(name = "account_number", nullable = false, length = 20)
	private String accountNumber;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CustomerStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) createdAt = Instant.now();
		if (balance == null) balance = BigDecimal.ZERO;
		if (status == null) status = CustomerStatus.ACTIVE;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Customer customer = (Customer) o;
		return Objects.equals(id, customer.id) &&
				Objects.equals(username, customer.username) &&
				Objects.equals(mobileNumber, customer.mobileNumber) &&
				Objects.equals(password, customer.password) &&
				Objects.equals(accountNumber, customer.accountNumber) &&
				Objects.equals(balance, customer.balance) &&
				status == customer.status &&
				Objects.equals(createdAt, customer.createdAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username, mobileNumber, password, accountNumber, balance, status, createdAt);
	}

	@Override
	public String toString() {
		return "Customer{" +
				"id=" + id +
				", username='" + username + '\'' +
				", mobileNumber='" + mobileNumber + '\'' +
				", password='" + password + '\'' +
				", accountNumber='" + accountNumber + '\'' +
				", balance=" + balance +
				", status=" + status +
				", createdAt=" + createdAt +
				'}';
	}
}
