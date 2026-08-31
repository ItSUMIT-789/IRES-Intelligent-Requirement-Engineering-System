package com.ires.auth.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "a-development-secret-with-at-least-32-bytes";

    private final JwtService jwtService = new JwtService(SECRET, 900_000);
    private final UserDetails user = User.withUsername("client@example.com")
            .password("hashed-password")
            .authorities("ROLE_CLIENT")
            .build();

    @Test
    void generatesAndValidatesTokenForUser() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("client@example.com");
        assertThat(jwtService.extractAuthorities(token)).containsExactly("ROLE_CLIENT");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void rejectsTokenSignedWithInvalidContent() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }
}
