package com.riskengine.data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Финансовая транзакция.
 * <p>
 * Зачем нужен: это основная "единица данных", которую будет анализировать ИИ-детектор.
 *
 * @param txId             - Уникальный ID транзакции
 * @param userId           - Ссылка на профиль пользователя
 * @param merchantId       - ID торговой точки
 * @param merchantCategory - MCC-код: категория бизнеса (еда, транспорт, развлечние)
 * @param amount           - Сумма операции
 * @param currency         - Валюта (пока только RUB)
 * @param timestamp        - Время операции в UTC
 * @param location         - Гео-метка (регион)
 */
public record Transaction(
        String txId,
        String userId,
        String merchantId,
        String merchantCategory,
        BigDecimal amount,
        String currency,
        Instant timestamp,
        String location
) {
    /**
     * Вспомогательный метод для создания "легитимной" транзакции с авто-генерацией ID. Упрощает код генератора — не нужно каждый раз писать
     * UUID.randomUUID().toString()
     */
    public static Transaction legit(String userId, String merchantId, String mcc,
                                    BigDecimal amount, Instant timestamp, String location) {
        return new Transaction(
                java.util.UUID.randomUUID().toString(),
                userId, merchantId, mcc, amount, "RUB", timestamp, location
        );
    }
}