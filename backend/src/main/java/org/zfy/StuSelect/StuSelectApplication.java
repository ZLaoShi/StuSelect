package org.zfy.StuSelect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class StuSelectApplication {

	public static void main(String[] args) {
		System.out.println("Current working directory: " + System.getProperty("user.dir"));
		SpringApplication.run(StuSelectApplication.class, args);
	}

}
