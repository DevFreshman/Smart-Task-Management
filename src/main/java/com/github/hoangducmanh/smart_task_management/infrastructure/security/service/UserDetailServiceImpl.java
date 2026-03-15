package com.github.hoangducmanh.smart_task_management.infrastructure.security.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findById(UserId.fromString(userId))
                .map(User -> new org.springframework.security.core.userdetails.User(
                        User.getEmail().value(),
                        User.getHashedPassword().value(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + User.getRole().name()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
