package org.example.productservice.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"org.example.productservice"})
@EntityScan(basePackages = {"org.example.productservice"})
@ComponentScan(basePackages = {"org.example.productservice"})
public class ProductConfiguration {
}
