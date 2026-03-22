package com.github.hoangducmanh.smart_task_management.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.github.hoangducmanh.smart_task_management")
@EnableJpaRepositories(
    basePackages = "com.github.hoangducmanh.smart_task_management.infrastructure.persistence"
)
@EntityScan(
    basePackages = "com.github.hoangducmanh.smart_task_management.infrastructure.persistence"
)
@EnableCaching
public class SmartTaskManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartTaskManagementApplication.class, args);
	}

}
