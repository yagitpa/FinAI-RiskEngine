package com.riskengine.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.riskengine.data.jpa.TransactionEntity;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, java.util.UUID> {
}