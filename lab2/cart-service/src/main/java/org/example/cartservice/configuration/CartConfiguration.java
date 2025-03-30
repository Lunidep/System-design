package org.example.cartservice.configuration;

import org.example.productservice.configuration.ProductConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"org.example.cartservice"})
@EntityScan(basePackages = {"org.example.cartservice"})
@ComponentScan(basePackages = {"org.example.cartservice"})
@Import(value = {ProductConfiguration.class})
public class CartConfiguration {
}
