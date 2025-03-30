package org.example.userservice.service.user;

import lombok.RequiredArgsConstructor;
import org.example.userservice.model.dto.security.UserCustom;
import org.example.userservice.repository.UserRepository;
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
