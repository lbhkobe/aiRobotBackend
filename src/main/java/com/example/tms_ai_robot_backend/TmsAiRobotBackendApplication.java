package com.example.tms_ai_robot_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TmsAiRobotBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TmsAiRobotBackendApplication.class, args);
	}

}
