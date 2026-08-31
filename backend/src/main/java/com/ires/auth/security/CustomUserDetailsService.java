package com.ires.auth.security;

import com.ires.user.entity.User;
import com.ires.user.entity.RoleName;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username.trim().toLowerCase();
        User user = userRepository.findByEmail(normalized)
                .or(() -> userRepository.findByUsernameIgnoreCase(normalized))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        String principalName = user.getUsername() == null ? user.getEmail() : user.getUsername();
        return org.springframework.security.core.userdetails.User.withUsername(principalName)
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> RoleName.valueOf(role.getName()))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .toList())
                .disabled(!user.isActive())
                .build();
    }
}
