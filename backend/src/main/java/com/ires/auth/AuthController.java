package com.ires.auth;

import com.ires.auth.dto.AuthResponse;
import com.ires.auth.security.JwtService;
import com.ires.common.response.ApiResponse;
import com.ires.user.dto.LoginRequest;
import com.ires.user.dto.RegisterRequest;
import com.ires.user.dto.UserResponse;
import com.ires.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Registration successful.", user));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        UserResponse user = userService.findByLogin(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists."));
        return ApiResponse.success("Login successful.", new AuthResponse(jwtService.generateToken(principal), user));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        UserResponse user = userService.findByLogin(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists."));
        return ApiResponse.success("Current user loaded.", user);
    }
}
