package com.ires.story.controller;

import com.ires.ai.service.AIAnalysisProvider;
import com.ires.auth.security.CustomUserDetailsService;
import com.ires.auth.security.JwtAuthenticationFilter;
import com.ires.auth.security.JwtService;
import com.ires.common.exception.GlobalExceptionHandler;
import com.ires.common.exception.ServiceUnavailableException;
import com.ires.config.SecurityConfig;
import com.ires.requirement.service.RequirementService;
import com.ires.story.service.UserStoryService;
import com.ires.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserStoryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserStoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserStoryService userStoryService;

    @MockBean
    private RequirementService requirementService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private AIAnalysisProvider analysisProvider;

    @Test
    void storyEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/requirements/" + UUID.randomUUID() + "/user-stories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void providerFailureReturnsServiceUnavailable() throws Exception {
        when(userStoryService.generate(any(), any()))
                .thenThrow(new ServiceUnavailableException("The AI provider is currently unavailable."));

        mockMvc.perform(post("/api/v1/requirements/" + UUID.randomUUID() + "/user-stories/generate")
                        .with(user("client@example.com").roles("CLIENT")))
                .andExpect(status().isServiceUnavailable());
    }
}
