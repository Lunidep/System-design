package org.example.lab2.configuration;

import org.example.lab2.configuration.security.InitialAuthenticationFilter;
import org.example.lab2.configuration.security.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


@Configuration
public class WebConfiguration {
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
												   InitialAuthenticationFilter initialAuthenticationFilter,
												   JwtAuthorizationFilter jwtAuthorizationFilter) throws Exception {

		http.addFilterAt(initialAuthenticationFilter, BasicAuthenticationFilter.class).addFilterAt(jwtAuthorizationFilter, BasicAuthenticationFilter.class);

		http.authorizeHttpRequests(authz -> authz
				.requestMatchers("/h2-console/**").permitAll()
				.anyRequest().authenticated());

		http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
				.csrf(AbstractHttpConfigurer::disable)
				.cors(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
