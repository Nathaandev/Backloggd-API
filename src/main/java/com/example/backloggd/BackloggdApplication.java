package com.example.backloggd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackloggdApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackloggdApplication.class, args);
	}

}
