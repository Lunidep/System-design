package org.example.lab2.configuration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.lab2.model.dto.security.Jwt;
import org.example.lab2.service.security.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.example.lab2.model.enums.ClaimField.ROLE;
import static org.example.lab2.model.enums.ClaimField.USERNAME;
import static org.example.lab2.model.enums.HeaderValues.AUTHORIZATION;
import static org.example.lab2.model.enums.HeaderValues.BEARER;


@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		String authorizationKey = request.getHeader(AUTHORIZATION.getName());
		if (Optional.ofNullable(authorizationKey).isPresent() && authorizationKey.startsWith(BEARER.getName())) {
			authorizationKey = authorizationKey.replace(BEARER.getName(), "");

			try {
				if (jwtService.isValidJwt(new Jwt(authorizationKey))) {
					Claims claims = jwtService.getClaims(authorizationKey);
					String username = String.valueOf(claims.get(USERNAME.getName()));
					List roles = claims.get(ROLE.getName(), List.class);

					List<GrantedAuthority> authorities = (List<GrantedAuthority>) roles.stream()
							.map(role -> new SimpleGrantedAuthority(role.toString()))
							.toList();

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (JwtException e) {
				logger.error(e.getMessage());
				SecurityContextHolder.getContext().setAuthentication(null);
				response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			}
		}
		filterChain.doFilter(request, response);
	}


	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().equals("/login");
	}
}
