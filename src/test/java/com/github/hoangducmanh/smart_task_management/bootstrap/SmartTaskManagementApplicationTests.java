package com.github.hoangducmanh.smart_task_management.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.AbstractPostgresContainerTest;

@SpringBootTest
@ActiveProfiles("test")
class SmartTaskManagementApplicationTests extends AbstractPostgresContainerTest{

	@Test
	void contextLoads() {
	}

}
