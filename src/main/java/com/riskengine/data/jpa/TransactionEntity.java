package com.riskengine.data.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA-сущность для хранения транзакции в PostgreSQL.
 */
@Entity
@Getter
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tx_id", unique = true, nullable = false, length = 36)
    private String txId;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String currency; // RUB

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 50)
    private String label; // LEGIT, FRAUD, REVIEW

    @Column(length = 100)
    private String reason;

    // JPA требует no-arg конструктор
    protected TransactionEntity() {
    }

    // Конструктор для маппинга из домена
    public static TransactionEntity from(com.riskengine.data.LabeledTransaction labeledTx) {
        TransactionEntity entity = new TransactionEntity();
        entity.txId = labeledTx.transaction().txId();
        entity.userId = labeledTx.transaction().userId();
        entity.amount = labeledTx.transaction().amount();
        entity.currency = labeledTx.transaction().currency();
        entity.timestamp = labeledTx.transaction().timestamp();
        entity.label = labeledTx.label().name();
        entity.reason = labeledTx.reason();
        return entity;
    }
}