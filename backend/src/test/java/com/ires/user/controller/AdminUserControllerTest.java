package com.ires.user.controller;

import com.ires.auth.security.CustomUserDetailsService;
import com.ires.auth.security.JwtAuthenticationFilter;
import com.ires.common.exception.GlobalExceptionHandler;
import com.ires.config.SecurityConfig;
import com.ires.user.dto.AdminUserResponse;
import com.ires.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void letRequestsPassThroughMockedJwtFilter() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void adminLoadsRealCanonicalUsersWithoutPasswords() throws Exception {
        when(userService.findAllForAdmin()).thenReturn(List.of(
                new AdminUserResponse(UUID.randomUUID(), "Ada", "Lovelace", "Ada Lovelace",
                        "ada@example.com", null, "BUSINESS_ANALYST", "Business Analyst",
                        true, Instant.parse("2026-01-01T00:00:00Z")),
                new AdminUserResponse(UUID.randomUUID(), "Grace", "Hopper", "Grace Hopper",
                        "grace@example.com", "grace", "DEVELOPER", "Developer",
                        true, Instant.parse("2026-01-02T00:00:00Z"))
        ));

        mockMvc.perform(get("/api/v1/admin/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].role").value("BUSINESS_ANALYST"))
                .andExpect(jsonPath("$.data[0].roleLabel").value("Business Analyst"))
                .andExpect(jsonPath("$..password").doesNotExist());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void everyNonAdminRoleIsForbidden() throws Exception {
        for (String role : List.of("CLIENT", "BUSINESS_ANALYST", "DEVELOPER", "TESTER")) {
            mockMvc.perform(get("/api/v1/admin/users").with(user(role.toLowerCase()).roles(role)))
                    .andExpect(status().isForbidden());
        }
    }
}
