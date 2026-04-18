package com.riskengine.engine;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riskengine.data.LabeledTransaction;

/**
 * Сервис отправки транзакций в Kafka.
 * <p>
 * Зачем нужен: 1. Отделяет логику генерации от логики отправки (Single Responsibility) 2. Инкапсулирует сериализацию и обработку ошибок 3. Легко
 * мокается в тестах
 */
@Service
@RequiredArgsConstructor
public class TransactionProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.transactions}")
    private String topic;

    /**
     * Отправляет одну транзакцию в топик.
     */
    public void send(LabeledTransaction labeledTx) {
        try {
            String json = objectMapper.writeValueAsString(labeledTx);

            kafkaTemplate.send(topic, labeledTx.transaction().txId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            System.out.printf("Отправлено в Kafka [%s] | Partition: %d | Offset: %d%n",
                                    labeledTx.transaction().txId(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            System.err.println("Ошибка отправки в Kafka: " + ex.getMessage());
                        }
                    });

        } catch (JsonProcessingException e) {
            System.err.println("Ошибка сериализации транзакции: " + e.getMessage());
        }
    }
}