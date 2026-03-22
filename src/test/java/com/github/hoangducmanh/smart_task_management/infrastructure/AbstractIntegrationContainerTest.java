package com.github.hoangducmanh.smart_task_management.infrastructure;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;

@Testcontainers
public abstract class AbstractIntegrationContainerTest {

    @Container
    @ServiceConnection
    protected static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16");

    @Container
    @ServiceConnection
    protected static RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse("redis:7-alpine"));
}