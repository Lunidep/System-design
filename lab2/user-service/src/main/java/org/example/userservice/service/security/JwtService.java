package org.example.userservice.service.security;

import io.jsonwebtoken.Claims;
import org.example.userservice.model.dto.security.Jwt;
import org.springframework.security.core.Authentication;


public interface JwtService {
    String generatedJwt(Authentication authentication);

    Claims getClaims(String jwt);

    boolean isValidJwt(Jwt jwt);
}