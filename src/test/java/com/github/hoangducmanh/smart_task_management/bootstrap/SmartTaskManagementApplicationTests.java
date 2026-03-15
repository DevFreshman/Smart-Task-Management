package com.github.hoangducmanh.smart_task_management.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {
    "JWT_SECRET_KEY=test-secret-key-for-testing-only-must-be-long-enough-256bits",
    "JWT_EXPIRATION_MS=900000",
    "REFRESH_TOKEN_EXPIRATION_MS=604800000"
})
class SmartTaskManagementApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgreSQLContainer 
	= new PostgreSQLContainer<>("postgres:16");
	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
	}
	@Test
	void contextLoads() {
	}

}
