package com.gitlab.mihnea_purcaru1.service_ticketsense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceTicketsenseApplication {

	static void main(String[] args) {
		SpringApplication.run(ServiceTicketsenseApplication.class, args);
	}

}
