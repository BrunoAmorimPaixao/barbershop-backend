package com.barbershop.api.response;

public record ClientResponse(
        Long id,
        String name,
        String email,
        String phone
) {
}
