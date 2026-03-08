package com.barbershop.api.request;

import com.barbershop.model.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ShopServiceRequest(
        @NotBlank String name,
        @NotNull ServiceType type,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull Integer durationMinutes
) {
}
