package com.barbershop.api.request;

import jakarta.validation.constraints.NotBlank;

public record BarberRequest(
        @NotBlank String name,
        String specialty
) {
}
