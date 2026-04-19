package com.riskengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.riskengine.data.LabeledTransaction;
import com.riskengine.data.RiskAuditRepository;
import com.riskengine.data.Transaction;
import com.riskengine.data.TransactionRepository;
import com.riskengine.engine.TransactionProducer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Интеграционный тест полного пайплайна: Producer → Kafka → Consumer → RiskEngine → PostgreSQL
 * <p>
 * Контейнеры поднимаются автоматически, тест изолирован от dev-окружения.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
class TransactionPipelineIT {

    // 1. Поднимаем легковесные контейнеры (статичные = один раз на весь класс тестов)
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
    );

    // 2. Динамически переопределяем application.yml на лету
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        // Отключаем генератор, чтобы не мешал тесту
        registry.add("app.data-generator.enabled", () -> "false");
    }

    @Autowired
    private TransactionProducer producer;

    @Autowired
    private TransactionRepository txRepo;

    @Autowired
    private RiskAuditRepository auditRepo;

    @Test
    void shouldProcessTransactionAndSaveToDb() {
        // Given: Создаём тестовую транзакцию с меткой FRAUD
        LabeledTransaction testTx = createTestTransaction("TEST-001", LabeledTransaction.Label.FRAUD);
        producer.send(testTx);

        // When & Then: Ждём асинхронной обработки и проверяем БД
        // Thread.sleep() запрещён. Использую Awaitility.
        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    // Проверяем транзакцию
                    var txInDb = txRepo.findAll().stream()
                            .filter(e -> e.getTxId().equals("TEST-001"))
                            .findFirst();

                    assertThat(txInDb)
                            .as("Транзакция должна быть сохранена в БД")
                            .isPresent()
                            .satisfies(e -> {
                                assertThat(e.get().getLabel()).isEqualTo("FRAUD");
                                assertThat(e.get().getAmount()).isEqualByComparingTo(new BigDecimal("2500.00"));
                            });

                    // Проверяем аудит-лог решения
                    var auditInDb = auditRepo.findAll().stream()
                            .filter(a -> a.getTxId().equals("TEST-001"))
                            .findFirst();

                    assertThat(auditInDb)
                            .as("Решение скоринга должно быть записано")
                            .isPresent()
                            .satisfies(a -> {
                                assertThat(a.get().getAction()).isEqualTo("BLOCK");
                                assertThat(a.get().getReason()).contains("fraud");
                            });
                });
    }

    private LabeledTransaction createTestTransaction(String txId, LabeledTransaction.Label label) {
        var tx = new Transaction(
                txId, "user-test-1", "merchant-1", "FOOD",
                new BigDecimal("2500.00"), "RUB",
                Instant.now(), "MOSCOW"
        );
        return new LabeledTransaction(tx, label, "test_pattern");
    }
}