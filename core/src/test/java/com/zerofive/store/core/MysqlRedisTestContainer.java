package com.zerofive.store.core;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

@ActiveProfiles("test")
public class MysqlRedisTestContainer {

    protected static MySQLContainer<?> mysql;
    protected static GenericContainer<?> redis;

    static {
        mysql = new MySQLContainer<>("mysql:8.4.5")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");

        redis = new GenericContainer<>("redis:7.2")
                .withExposedPorts(6379);

        mysql.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("jwt.secret", () -> "testsecrettestsecrettestsecrettestsecret");
    }
}
