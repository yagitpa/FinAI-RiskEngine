package com.riskengine.web;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.riskengine.data.LabeledTransaction;
import com.riskengine.data.TransactionGenerator;

import java.util.stream.Collectors;

/**
 * Запускается автоматически после старта Spring Boot. Демонстрирует работу генератора синтетических данных.
 */
@Component
public class DataGeneratorRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("Запуск генератора синтетических данных...");

        TransactionGenerator generator = new TransactionGenerator(42L, 50);

        var transactions = generator.generate(20, 0.1)
                .toList();

        System.out.println("\n📊 Сгенерировано транзакций: " + transactions.size());
        transactions.forEach(tx ->
                System.out.printf("  [%s] %s | %s | %.2f RUB | %s%n",
                        tx.label(),
                        tx.transaction().timestamp(),
                        tx.transaction().merchantCategory(),
                        tx.transaction().amount(),
                        tx.reason()
                )
        );

        // Подсчёт статистики
        long fraudCount = transactions.stream()
                .filter(tx -> tx.label() == LabeledTransaction.Label.FRAUD)
                .count();
        System.out.printf("%n Легитимных: %d, Мошеннических: %d%n",
                transactions.size() - fraudCount, fraudCount);
    }
}