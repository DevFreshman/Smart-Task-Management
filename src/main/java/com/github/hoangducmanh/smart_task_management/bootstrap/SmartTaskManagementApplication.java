package com.github.hoangducmanh.smart_task_management.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.hoangducmanh.smart_task_management")
public class SmartTaskManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartTaskManagementApplication.class, args);
	}

}
