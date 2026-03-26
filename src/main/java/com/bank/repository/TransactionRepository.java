package com.bank.repository;
import com.bank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
	List<Transaction> findByAccountNumberOrderByTransactionDateDesc(String accountNumber);

	// Custom query for searching transactions by filters
	// This will be implemented in AccountServiceImpl using CriteriaBuilder or Specification
}
