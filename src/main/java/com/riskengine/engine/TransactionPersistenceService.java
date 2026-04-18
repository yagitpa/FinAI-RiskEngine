package com.riskengine.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.riskengine.data.LabeledTransaction;
import com.riskengine.data.RiskAuditRepository;
import com.riskengine.data.TransactionRepository;
import com.riskengine.data.jpa.RiskAuditEntity;
import com.riskengine.data.jpa.TransactionEntity;

/**
 * Сервис атомарного сохранения транзакции и решения скоринга.
 *
 * @Transactional гарантирует: 1. Обе записи выполняются в одной транзакции БД 2. При ошибке любой из операций происходит ROLLBACK 3. Данные не
 * попадают в "половинчатом" состоянии
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionPersistenceService {

    private final TransactionRepository txRepo;
    private final RiskAuditRepository auditRepo;

    @Transactional
    public void saveWithAudit(LabeledTransaction labeledTx, RiskScoringEngine.RiskDecision decision) {
        // 1. Маппинг домена → JPA-сущности
        TransactionEntity txEntity = TransactionEntity.from(labeledTx);
        RiskAuditEntity auditEntity = RiskAuditEntity.from(labeledTx.transaction().txId(), decision);

        // 2. Сохранение
        txRepo.save(txEntity);
        auditRepo.save(auditEntity);

        log.debug("Сохранено в БД: tx={} | action={}", labeledTx.transaction().txId(), decision.action());
    }
}