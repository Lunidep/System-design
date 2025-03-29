package org.example.lab2.service;

import org.example.lab2.model.UserCustom;
import org.example.lab2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return new UserCustom(userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("USER NOT FOUND!")));
	}
}
