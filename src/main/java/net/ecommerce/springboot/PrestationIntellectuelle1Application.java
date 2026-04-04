package net.ecommerce.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
public class PrestationIntellectuelle1Application {

	public static void main(String[] args) {
		SpringApplication.run(PrestationIntellectuelle1Application.class, args);
	}

}
