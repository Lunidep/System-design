package org.example.lab2.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.lab2.model.dto.UserDto;
import org.example.lab2.service.JwtServiceImpl;
import org.example.lab2.service.user.UsernamePasswordAuthenticationProvider;
import org.hibernate.ObjectNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.example.lab2.model.enums.HeaderValues.AUTHORIZATION;
import static org.example.lab2.model.enums.HeaderValues.BEARER;

@Component
@RequiredArgsConstructor
public class InitialAuthenticationFilter extends OncePerRequestFilter {
	private final JwtServiceImpl jwtService;
	private final UsernamePasswordAuthenticationProvider authenticationProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
		if (request.getHeader(AUTHORIZATION.getName()) == null) {
			String bodyJson = request.getReader().readLine();
			if (bodyJson != null) {
				ObjectMapper mapper = new ObjectMapper();
				UserDto userDto = mapper.readValue(bodyJson, UserDto.class);
				String username = userDto.getUsername();
				String password = userDto.getPassword();
				try {
					Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
					authentication = authenticationProvider.authenticate(authentication);
					String jwt = jwtService.generatedJwt(authentication);
					response.setHeader(AUTHORIZATION.getName(), BEARER.getName() + " " + jwt);
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