package com.riskengine.data;

/**
 * Транзакция с меткой для обучения/тестирования ИИ.
 * <p>
 * Зачем нужен: чтобы сравнивать решение ИИ с "правильным ответом".
 *
 * @param transaction транзакция
 * @param label       метка LEGIT, FRAUD, REVIEW
 * @param reason      человекочитаемое объяснение
 */
public record LabeledTransaction(
        Transaction transaction,
        Label label,
        String reason
) {
    /**
     * Возможные метки.
     * <p>
     * LEGIT — нормальная операция FRAUD — явно мошенническая (по правилам генератора) REVIEW — подозрительная, требует ручной проверки (серая зона)
     */
    public enum Label {
        LEGIT, FRAUD, REVIEW
    }

    /**
     * Удобный метод для создания легитимной транзакции.
     */
    public static LabeledTransaction legit(Transaction tx) {
        return new LabeledTransaction(tx, Label.LEGIT, "normal_pattern");
    }

    /**
     * Удобный метод для создания мошеннической транзакции.
     */
    public static LabeledTransaction fraud(Transaction tx, String reason) {
        return new LabeledTransaction(tx, Label.FRAUD, reason);
    }
}