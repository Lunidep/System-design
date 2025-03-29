package org.example.lab2.controller;

import org.example.lab2.model.User;
import org.example.lab2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserRepository userRepository;

	@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
	@GetMapping("/get/myname")
	public ResponseEntity<String> getName() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
	}

	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@GetMapping("/get/{id}")
	public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(userRepository.findById(id).orElseThrow(() ->
				new UsernameNotFoundException("User with id = " + id + " not found!")));
	}

	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@GetMapping("/get/all")
	public ResponseEntity<List<User>> getAllUser() {
		return ResponseEntity.status(HttpStatus.OK).body(userRepository.findAll());
	}
}
