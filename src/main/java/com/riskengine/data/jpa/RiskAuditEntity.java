package com.riskengine.data.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Аудит-лог принятия решений. Хранит только результат скоринга. Никогда не изменяется.
 */
@Entity
@Getter
@Table(name = "risk_audit")
public class RiskAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tx_id", nullable = false, length = 36)
    private String txId;

    @Column(nullable = false, length = 20)
    private String action; // BLOCK, REVIEW, ALLOW

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Instant evaluatedAt;

    protected RiskAuditEntity() {
    }

    public static RiskAuditEntity from(String txId, com.riskengine.engine.RiskScoringEngine.RiskDecision decision) {
        RiskAuditEntity entity = new RiskAuditEntity();
        entity.txId = txId;
        entity.action = decision.action();
        entity.reason = decision.reason();
        entity.evaluatedAt = Instant.now();
        return entity;
    }
}