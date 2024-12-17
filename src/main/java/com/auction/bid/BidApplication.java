package com.auction.bid;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class BidApplication implements CommandLineRunner {

	private final ApplicationContext context;

	public BidApplication(ApplicationContext context) {
		this.context = context;
	}

	public static void main(String[] args) {
		SpringApplication.run(BidApplication.class, args);
	}

	@Override
	public void run(String... args) {
		System.out.println("Registered Beans:");
		for (String beanName : context.getBeanDefinitionNames()) {
				if (beanName.contains("productServiceImpl")) {
					System.out.println("Found Bean: " + beanName);
				}
			}
		}
	}
