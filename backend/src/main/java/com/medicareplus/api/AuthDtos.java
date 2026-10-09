package com.medicareplus.api;

import com.medicareplus.domain.Enums.Role;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn, UserView user) {}
    public record UserView(Long id, String username, String nomComplet, Role role) {}
}
