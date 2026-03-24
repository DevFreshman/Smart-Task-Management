package com.github.hoangducmanh.smart_task_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SmartTaskManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartTaskManagementApplication.class, args);
	}

}
