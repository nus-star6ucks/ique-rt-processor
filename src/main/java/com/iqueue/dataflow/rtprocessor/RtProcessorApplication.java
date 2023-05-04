package com.iqueue.dataflow.rtprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RtProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(RtProcessorApplication.class, args);
	}
}
