package com.riskengine.data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.function.Function;

/**
 * Паттерн аномалии — интерфейс для трансформации транзакции.
 */
public interface AnomalyPattern extends Function<Transaction, Transaction> {

    /**
     * Человекочитаемое описание паттерна (для логов и отладки).
     */
    String description();

    // === ПРЕДОПРЕДЕЛЁННЫЕ ПАТТЕРНЫ ===

    /**
     * @classdesc Анонимный класс с реализацией одного из предопределённых шаблонов анонмальности - невозможного путешествия по России
     */
    AnomalyPattern IMPOSSIBLE_TRAVEL = new AnomalyPattern() {
        @Override
        public Transaction apply(Transaction tx) {
            String fakeLocation = switch (tx.location()) {
                case "MOSCOW" -> "VLADIVOSTOK";
                case "VLADIVOSTOK" -> "MOSCOW";
                case "SPB" -> "EKATERINBURG";
                default -> "UNKNOWN_REGION";
            };
            return new Transaction(
                    tx.txId(), tx.userId(), tx.merchantId(), tx.merchantCategory(),
                    tx.amount(), tx.currency(), tx.timestamp(), fakeLocation
            );
        }

        @Override
        public String description() {
            return "impossible_travel";
        }
    };

    /**
     * @classdesc Анонимный класс с реализацией одного из предопределённых шаблонов анонмальности - увеличение суммы
     */
    AnomalyPattern AMOUNT_SPIKE = new AnomalyPattern() {
        private final Random random = new Random(42);

        @Override
        public Transaction apply(Transaction tx) {
            BigDecimal multiplier = BigDecimal.valueOf(3.5 + random.nextDouble() * 2);
            return new Transaction(
                    tx.txId(), tx.userId(), tx.merchantId(), tx.merchantCategory(),
                    tx.amount().multiply(multiplier), tx.currency(), tx.timestamp(), tx.location()
            );
        }

        @Override
        public String description() {
            return "amount_spike";
        }
    };

    /**
     * @classdesc Анонимный класс с реализации шаблона «burst» - очень быстрый поток транзакций
     */
    AnomalyPattern VELOCITY_BURST = new AnomalyPattern() {
        @Override
        public Transaction apply(Transaction tx) {
            Instant fakeTime = tx.timestamp().minusSeconds(1 + new Random(42).nextInt(3));
            return new Transaction(
                    tx.txId(), tx.userId(), tx.merchantId(), tx.merchantCategory(),
                    tx.amount(), tx.currency(), fakeTime, tx.location()
            );
        }

        @Override
        public String description() {
            return "velocity_burst";
        }
    };

    /**
     * Вспомогателььный метод для получения случайного шаблона аномальности
     *
     * @param random
     *
     * @return случайный паттерн
     */
    static AnomalyPattern random(Random random) {
        AnomalyPattern[] patterns = values();
        return patterns[random.nextInt(patterns.length)];
    }

    /**
     * Вспомогательский метод для получения всех паттернов
     *
     * @return массив паттернов
     */
    private static AnomalyPattern[] values() {
        return new AnomalyPattern[] {IMPOSSIBLE_TRAVEL, AMOUNT_SPIKE, VELOCITY_BURST};
    }
}