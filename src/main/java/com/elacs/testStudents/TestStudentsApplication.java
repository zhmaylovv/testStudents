package com.elacs.testStudents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestStudentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestStudentsApplication.class, args);
	}

}
