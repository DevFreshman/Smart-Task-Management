package com.github.hoangducmanh.smart_task_management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.hoangducmanh.smart_task_management.infrastructure.AbstractIntegrationContainerTest;

@SpringBootTest
@ActiveProfiles("test")
class SmartTaskManagementApplicationTests extends AbstractIntegrationContainerTest{

	@Test
	void contextLoads() {
	}

}
