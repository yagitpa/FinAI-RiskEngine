package com.riskengine.config;

import lombok.RequiredArgsConstructor;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Collections;
import java.util.Properties;

/**
 * Автоматическое создание топиков при старте приложения.
 * <p>
 * Зачем нужно: - Гарантирует, что топик существует до начала отправки сообщений - Позволяет задать параметры: партиции, репликация, время хранения -
 * Не зависит от настроек брокера
 */
@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topic.transactions}")
    private String topicName;

    /**
     * Создаёт AdminClient для управления топиками
     */
    @Bean
    public AdminClient adminClient() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        return AdminClient.create(props);
    }

    /**
     * CommandLineRunner выполняется после старта контекста. Проверяет наличие топика и создаёт его, если нужно.
     */
    @Bean
    public CommandLineRunner createTopicsRunner(AdminClient adminClient) {
        return args -> {
            var topics = adminClient.listTopics().names().get();
            if (!topics.contains(topicName)) {
                System.out.println("Создаю топик Kafka: " + topicName);

                NewTopic newTopic = TopicBuilder.name(topicName)
                        .partitions(3)
                        .replicas(1)
                        .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L))
                        .build();

                adminClient.createTopics(Collections.singletonList(newTopic))
                        .all()
                        .get();

                System.out.println("Топик '" + topicName + "' создан");
            } else {
                System.out.println("Топик '" + topicName + "' уже существует");
            }
        };
    }
}