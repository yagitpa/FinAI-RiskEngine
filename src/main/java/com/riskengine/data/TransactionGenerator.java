package com.riskengine.data;

import net.datafaker.Faker;
import net.datafaker.service.RandomService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Генератор синтетических транзакций.
 * <p>
 * Ключевые принципы: 1. Детерминированность: при одинаковом seed — одинаковый результат (для тестов) 2. Потоковая обработка: генерируем данные "на
 * лету", не загружая память 3. Реалистичность: используем DataFaker для правдоподобных значений
 */
public class TransactionGenerator {

    private final Random random;
    private final Faker faker;
    private final List<UserProfile> users;

    /**
     * Конструктор.
     *
     * @param seed      — число для инициализации генератора случайных чисел. Одинаковый seed = одинаковые данные = воспроизводимость тестов.
     * @param userCount — сколько профилей пользователей создать.
     */
    public TransactionGenerator(long seed, int userCount) {
        this.random = new Random(seed);
        this.faker = new Faker(new RandomService(random).getRandomInternal());
        this.users = generateUsers(userCount);
    }

    /**
     * Генерация списка пользователей. Упрощённо: случайные регионы, возрастные группы, сегменты риска.
     */
    private List<UserProfile> generateUsers(int count) {
        List<UserProfile> list = new ArrayList<>(count);
        String[] regions = {"MOSCOW", "SPB", "VLADIVOSTOK", "EKATERINBURG", "KAZAN"};

        for (int i = 0; i < count; i++) {
            list.add(new UserProfile(
                    UUID.randomUUID().toString(),
                    regions[random.nextInt(regions.length)],
                    UserProfile.AgeTier.values()[random.nextInt(3)],
                    UserProfile.RiskSegment.values()[random.nextInt(3)]
            ));
        }
        return list;
    }

    /**
     * Публичный API: сгенерировать поток транзакций.
     *
     * @param count     — сколько транзакций нужно
     * @param fraudRate — доля мошеннических операций (0.0 .. 1.0)
     *
     * @return Stream<LabeledTransaction>
     */
    public Stream<LabeledTransaction> generate(int count, double fraudRate) {
        return Stream.generate(() -> nextTransaction(fraudRate))
                .limit(count);
    }

    /**
     * Генерация одной транзакции. Внутренний метод, не для публичного использования.
     */
    private LabeledTransaction nextTransaction(double fraudRate) {
        UserProfile user = users.get(random.nextInt(users.size()));
        Transaction baseTx = buildLegitTransaction(user);

        if (random.nextDouble() < fraudRate) {
            AnomalyPattern pattern = AnomalyPattern.random(random);
            Transaction fraudTx = pattern.apply(baseTx);
            return LabeledTransaction.fraud(fraudTx, pattern.description());
        }

        return LabeledTransaction.legit(baseTx);
    }

    /**
     * Сборка легитимной транзакции для пользователя. Использует DataFaker для реалистичных значений.
     */
    private Transaction buildLegitTransaction(UserProfile user) {
        // Типичная сумма: 100-5000₽, логнормальное распределение (больше мелких, меньше крупных)
        BigDecimal amount = BigDecimal.valueOf(
                Math.round(100 + Math.exp(random.nextGaussian() * 1.5) * 500)
        );

        // Случайная категория бизнеса (MCC)
        String[] mccs = {"FOOD", "TRANSPORT", "ENTERTAINMENT", "RETAIL", "SERVICES"};
        String mcc = mccs[random.nextInt(mccs.length)];

        // Время: сейчас минус случайное количество часов (0-72)
        Instant timestamp = Instant.now().minus(random.nextInt(72), ChronoUnit.HOURS);

        return Transaction.legit(
                user.id(),
                "MERCH_" + random.nextInt(1000),
                mcc,
                amount,
                timestamp,
                user.region() // по умолчанию транзакция в регионе пользователя
        );
    }
}