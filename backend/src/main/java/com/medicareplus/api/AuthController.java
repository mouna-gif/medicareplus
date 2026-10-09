package com.medicareplus.api;

import static com.medicareplus.api.AuthDtos.*;

import com.medicareplus.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Database-backed JWT authentication and token lifecycle")
public class AuthController {
    private final AuthService auth;
    @Operation(summary = "Authenticate a MediCare+ user", description = "Verifies a BCrypt password against MySQL and returns access and refresh JWTs.")
    @SecurityRequirements
    @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @Operation(summary = "Rotate a refresh token", description = "Validates the persisted refresh-token hash and returns a rotated token pair.")
    @SecurityRequirements
    @PostMapping("/refresh") AuthResponse refresh(@Valid @RequestBody RefreshRequest request) { return auth.refresh(request); }
    @Operation(summary = "Log out the authenticated user", description = "Revokes the persisted refresh token.", responses = @ApiResponse(responseCode = "204", description = "Refresh token revoked"))
    @PostMapping("/logout") ResponseEntity<Void> logout(Principal principal) {
        auth.logout(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
