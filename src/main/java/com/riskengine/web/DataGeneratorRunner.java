package com.riskengine.web;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.riskengine.data.TransactionGenerator;
import com.riskengine.engine.TransactionProducer;

/**
 * Демонстрирует потоковую генерацию и отправку в Kafka.
 */
@Component
@RequiredArgsConstructor
public class DataGeneratorRunner implements CommandLineRunner {

    private final TransactionProducer producer;

    @Override
    public void run(String... args) {
        System.out.println("Запуск генератора + отправка в Kafka...");

        TransactionGenerator generator = new TransactionGenerator(42L, 50);

        generator.generate(30, 0.15)
                .forEach(tx -> {
                    producer.send(tx);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                });

        System.out.println("\nГенерация завершена. Данные в топике 'transactions-risk'.");
    }
}