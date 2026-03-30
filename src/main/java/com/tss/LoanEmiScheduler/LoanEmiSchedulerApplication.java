package com.tss.LoanEmiScheduler;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LoanEmiSchedulerApplication {
	public static void main(String[] args) throws Exception {
		Dotenv dotenv = Dotenv.load();

		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		System.setProperty("EMAIL", dotenv.get("EMAIL"));
		System.setProperty("EMAIL_APP_PASSWORD", dotenv.get("EMAIL_APP_PASSWORD"));

		ConfigurableApplicationContext context = SpringApplication.run(LoanEmiSchedulerApplication.class, args);
	}
}
