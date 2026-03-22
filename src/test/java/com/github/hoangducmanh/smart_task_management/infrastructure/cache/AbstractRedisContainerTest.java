package com.github.hoangducmanh.smart_task_management.infrastructure.cache;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;


@Testcontainers
public abstract class AbstractRedisContainerTest {
    @Container
    @ServiceConnection
    protected static RedisContainer redisContainer = new RedisContainer(
        DockerImageName.parse("redis:7-alpine")
    );
}
