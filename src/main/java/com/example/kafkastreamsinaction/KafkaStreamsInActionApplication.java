package com.example.kafkastreamsinaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class KafkaStreamsInActionApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsInActionApplication.class, args);
	}

}
