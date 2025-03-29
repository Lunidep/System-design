package org.example.lab2.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.lab2.dto.UserDto;
import org.example.lab2.model.UsernamePasswordAuthentication;
import org.example.lab2.service.HeaderValues;
import org.example.lab2.service.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.ObjectNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class InitialAuthenticationFilter extends OncePerRequestFilter {
	private final JwtServiceImpl jwtService;
	private final UsernamePasswordAuthenticationProvider authenticationProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
		if (request.getHeader("Authorization") == null) {
			String bodyJson = request.getReader().readLine();
			if (bodyJson != null) {
				ObjectMapper mapper = new ObjectMapper();
				UserDto userDto = mapper.readValue(bodyJson, UserDto.class);
				String username = userDto.getUsername();
				String password = userDto.getPassword();
				try {
					Authentication authentication = new UsernamePasswordAuthentication(username, password);
					authentication = authenticationProvider.authenticate(authentication);
					String jwt = jwtService.generatedJwt(authentication);
					response.setHeader("Authorization", HeaderValues.BEARER + jwt);
				} catch (BadCredentialsException | ObjectNotFoundException e) {
					logger.error(e.getMessage());
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
			}
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getServletPath().equals("/login");
	}
}