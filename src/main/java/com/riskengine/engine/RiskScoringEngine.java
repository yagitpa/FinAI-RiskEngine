package com.riskengine.engine;

import org.springframework.stereotype.Service;
import com.riskengine.data.LabeledTransaction;

import java.math.BigDecimal;

/**
 * Сервис оценки риска транзакции.
 * <p>
 * Зачем нужен: - Инкапсулирует бизнес-правила отдельно от инфраструктуры Kafka - Легко заменяется на ИИ-агент в будущем (полиморфизм/интерфейсы) -
 * Тестируется unit-тестами без поднятия Kafka
 */
@Service
public class RiskScoringEngine {

    // Порог суммы, выше которого транзакция требует ручной проверки
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("15000");

    /**
     * Оценивает транзакцию и возвращает решение.
     */
    public RiskDecision evaluate(LabeledTransaction tx) {
        String txId = tx.transaction().txId();

        // 1. Проверка явной метки фрода (от генератора)
        if (tx.label() == LabeledTransaction.Label.FRAUD) {
            return new RiskDecision(txId, "BLOCK", "Explicit fraud pattern detected");
        }

        // 2. Проверка суммы (правило бизнес-логики)
        if (tx.transaction().amount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            return new RiskDecision(txId, "REVIEW", "Amount exceeds safety threshold");
        }

        // 3. По умолчанию — пропускаем
        return new RiskDecision(txId, "ALLOW", "Normal transaction pattern");
    }

    public record RiskDecision(String txId, String action, String reason) {
    }
}