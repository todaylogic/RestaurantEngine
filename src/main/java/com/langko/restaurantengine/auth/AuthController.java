package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.auth.dto.AuthResponse;
import com.langko.restaurantengine.auth.dto.LoginRequest;
import com.langko.restaurantengine.auth.dto.RegisterRequest;
import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.dto.StaffResponse;
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
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<StaffResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal Staff currentUser) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request, currentUser)));
    }
}
