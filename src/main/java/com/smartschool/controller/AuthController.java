package com.smartschool.controller;

import com.smartschool.dto.ApiResponse;
import com.smartschool.dto.AuthResponse;
import com.smartschool.dto.ChangePasswordRequest;
import com.smartschool.dto.LoginRequest;
import com.smartschool.security.UserPrincipal;
import com.smartschool.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        AuthResponse response = authService.login(request, ip);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                           @AuthenticationPrincipal UserPrincipal principal,
                                                           HttpServletRequest httpRequest) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("You must be logged in to change password"));
        }
        String ip = httpRequest.getRemoteAddr();
        authService.changePassword(principal.getId(), request, ip);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
