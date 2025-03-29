package org.example.lab2.service;

import org.example.lab2.model.Jwt;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;


public interface JwtService {
	String generatedJwt(Authentication authentication);
	Claims getClaims(String jwt);
	boolean isValidJwt(Jwt jwt);
}