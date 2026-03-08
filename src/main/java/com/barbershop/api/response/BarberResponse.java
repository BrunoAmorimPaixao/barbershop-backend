package com.barbershop.api.response;

public record BarberResponse(
        Long id,
        String name,
        String specialty
) {
}
