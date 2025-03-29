package org.example.lab2.service;

import io.jsonwebtoken.Claims;
import org.example.lab2.model.dto.Jwt;
import org.springframework.security.core.Authentication;


public interface JwtService {
	String generatedJwt(Authentication authentication);
	Claims getClaims(String jwt);
	boolean isValidJwt(Jwt jwt);
}