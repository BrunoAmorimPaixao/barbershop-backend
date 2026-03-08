package com.barbershop.api.response;

public record LoginResponse(
        String email,
        String displayName,
        String token
) {
}
