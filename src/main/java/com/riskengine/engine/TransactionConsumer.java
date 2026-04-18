package com.riskengine.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riskengine.data.LabeledTransaction;

/**
 * Компонент, слушающий топик Kafka в фоновом режиме.
 * <p>
 * Как работает: 1. Spring Kafka создаёт отдельный пул потоков для этой группы 2. При появлении сообщения в топике вызывает метод consume() 3. Метод
 * выполняется асинхронно, не блокируя основной поток приложения
 */
@Component
@RequiredArgsConstructor
@Slf4j //Использую SLF4J через Lombok для thread-safe логирования. В проде заменю на log.info(MDC.put("txId", ...))
public class TransactionConsumer {

    private final RiskScoringEngine scoringEngine;
    private final ObjectMapper objectMapper;
    private final TransactionPersistenceService persistenceService;

    /**
     * @KafkaListener — Spring Kafka. Параметры: - topics: откуда читать - groupId: к какой группе потребителей принадлежим - containerFactory:
     * использует автоконфигурацию Spring Boot (ConcurrentKafkaListenerContainerFactory)
     * <p>
     * Использую декларативный подход Spring Kafka. Контейнер сам управляет пулом потоков, commit offset и graceful shutdown
     * <p>
     * Одинаковый group-id делит партиции между инстансами. Если запустить 3 пода, каждый заберёт 1/3 потока.
     * <p>
     * Явная десериализация защищает от silent data corruption. Если схема изменится, я ловлю JsonProcessingException и шлю в DLT, отправляю
     * уведомление
     */
    @KafkaListener(
            topics = "${app.kafka.topic.transactions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        try {
            // 1. Десериализация JSON → Java Object
            LabeledTransaction tx = objectMapper.readValue(message, LabeledTransaction.class);

            log.debug("Received transaction: {} | Label: {}",
                    tx.transaction().txId(), tx.label());

            // 2. Запуск скоринга
            RiskScoringEngine.RiskDecision decision = scoringEngine.evaluate(tx);

            // 3. Логирование решения
            log.info("Risk Decision [{}] → ACTION: {} | REASON: {}",
                    decision.txId(), decision.action(), decision.reason());

            persistenceService.saveWithAudit(tx, decision);

        } catch (JsonProcessingException e) {
            log.error("Malformed Kafka message: {}", message, e);
        } catch (Exception e) {
            log.error("Unexpected error during transaction processing", e);
        }
    }
}