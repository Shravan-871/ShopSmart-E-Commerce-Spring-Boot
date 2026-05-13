package com.shopsmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShopsmartApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopsmartApplication.class, args);
	}

}
