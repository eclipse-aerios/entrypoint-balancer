package org.aeros;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import lombok.AllArgsConstructor;

@SpringBootApplication
@EnableFeignClients
@AllArgsConstructor
public class EntryPointBalancer {

	public static void main(String[] args) {
		run(EntryPointBalancer.class, args);
	}
}