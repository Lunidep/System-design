package org.example.lab2.service;

import org.example.lab2.model.Jwt;
import org.example.lab2.model.User;
import org.example.lab2.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
	@Value("${jwt.signing.key}")
	private String signingKey;
	@Value("${jwt.key.expiration}")
	private Long jwtExpiration;
	private final UserRepository userRepository;

	private SecretKey key;

	private SecretKey generatedSecretKey() {
		if (key == null) {
			key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
		}
		return key;
	}

	@Override
	public String generatedJwt(Authentication authentication) {
		return Jwts.builder()
				.setClaims(
						Map.of(
								ClaimField.USERNAME, authentication.getName(),
								ClaimField.ROLE, authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
								ClaimField.USER_ID, String.valueOf(userRepository.findByUsername(authentication.getName()).get().getId())))
				.setExpiration(new Date(new Date().getTime() + jwtExpiration))
				.setSubject(authentication.getName())
				.signWith(generatedSecretKey())
				.compact();
	}

	@Override
	public Claims getClaims(String jwt) {
		return Jwts.parserBuilder()
				.setSigningKey(generatedSecretKey())
				.build()
				.parseClaimsJws(jwt)
				.getBody();
	}

	@Override
	public boolean isValidJwt(Jwt jwt) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(generatedSecretKey())
				.build()
				.parseClaimsJws(jwt.getToken())
				.getBody();

		Optional<User> user = userRepository.findByUsername(String.valueOf(claims.get(ClaimField.USERNAME)));

		return claims.getExpiration().after(new Date()) && user.isPresent();
	}
}
