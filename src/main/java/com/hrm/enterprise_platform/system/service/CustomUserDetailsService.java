package com.hrm.enterprise_platform.system.service;

import com.hrm.enterprise_platform.system.entity.Role;
import com.hrm.enterprise_platform.system.entity.User;
import com.hrm.enterprise_platform.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại"));

        List<GrantedAuthority> authorities = user.getRoles().stream()
        .map(Role::getName)
        .map(r -> "ROLE_" + r)
        .map(SimpleGrantedAuthority::new)
        .map(a -> (GrantedAuthority) a)
        .collect(Collectors.toList());   // ✔ FIXED


        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }
}
