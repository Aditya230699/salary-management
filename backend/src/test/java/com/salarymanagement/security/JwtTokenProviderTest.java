package com.salarymanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "mySecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm2024");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 86400000L);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void generateToken_ValidAuth_ReturnsToken() {
        UserDetails userDetails = new User("hr_manager", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_HR_MANAGER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should extract username from token")
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        UserDetails userDetails = new User("hr_manager", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_HR_MANAGER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);
        String username = tokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("hr_manager");
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void validateToken_InvalidToken_ReturnsFalse() {
        assertThat(tokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Set very short expiration
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", -1000L);

        UserDetails userDetails = new User("hr_manager", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_HR_MANAGER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);

        assertThat(tokenProvider.validateToken(token)).isFalse();
    }
}
