package com.example.countrymanager;

import com.example.countrymanager.util.EntityConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableMongoAuditing
public class CountryManagerApplication {

	@Bean
	public EntityConverter entityConverter() {
		return new EntityConverter();
	}

	public static void main(String[] args) {
		SpringApplication.run(CountryManagerApplication.class, args);
	}

}
