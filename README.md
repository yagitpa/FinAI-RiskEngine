# FinAI Risk Engine

Гибридная система обнаружения мошенничества с генерацией синтетических данных и оценкой рисков транзакций.

## Стек

- Java 21 + Spring Boot 3.3.5
- Apache Kafka (потоковая обработка)
- PostgreSQL + pgvector (хранилище)
- Testcontainers (тесты)
- DataFaker (синтетические данные)

## Быстрый старт

```bash
# Запуск инфраструктуры (PostgreSQL + Kafka)
docker-compose up -d

# Запуск приложения
mvn spring-boot:run
```

## Архитектура

```
TransactionGenerator → Kafka Producer → Kafka → Consumer → Risk Engine → PostgreSQL
```

## Компоненты

- **TransactionGenerator** — генерация синтетических транзакций с паттернами аномалий
- **RiskScoringEngine** — оценка риска транзакции в реальном времени
- **Kafka Producer/Consumer** — потоковая передача событий
- **PostgreSQL** — хранение профилей, транзакций и результатов анализа

## Тесты

```bash
mvn test
```

## Конфигурация

Параметры подключения (application.properties):
- `spring.datasource.url=jdbc:postgresql://localhost:5432/riskdb`
- `spring.kafka.bootstrap-servers=localhost:9092`
- Пользователь БД: `riskuser` / пароль: `riskpass`

## Kafka UI

Доступна по адресу: http://localhost:8080
