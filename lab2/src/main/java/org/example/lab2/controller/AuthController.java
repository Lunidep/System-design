package org.example.lab2.controller;

import lombok.RequiredArgsConstructor;
import org.example.lab2.model.dto.UserDto;
import org.example.lab2.service.security.JwtServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.example.lab2.model.enums.HeaderValues.AUTHORIZATION;
import static org.example.lab2.model.enums.HeaderValues.BEARER;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtServiceImpl jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody UserDto userDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDto.getUsername(),
                        userDto.getPassword()
                )
        );

        String jwt = jwtService.generatedJwt(authentication);
        return ResponseEntity.ok()
                .header(AUTHORIZATION.getName(), BEARER.getName() + " " + jwt)
                .build();
    }
}
