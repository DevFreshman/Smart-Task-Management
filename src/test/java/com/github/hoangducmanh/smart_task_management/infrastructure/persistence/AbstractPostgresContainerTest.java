package com.github.hoangducmanh.smart_task_management.infrastructure.persistence;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractPostgresContainerTest {
    @Container
    @ServiceConnection
    protected static PostgreSQLContainer<?> postgreSQLContainer = 
    new PostgreSQLContainer<>("postgres:16");
}
