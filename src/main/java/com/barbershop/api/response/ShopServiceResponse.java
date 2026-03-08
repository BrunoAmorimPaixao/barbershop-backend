package com.barbershop.api.response;

import com.barbershop.model.ServiceType;

import java.math.BigDecimal;

public record ShopServiceResponse(
        Long id,
        String name,
        ServiceType type,
        BigDecimal price,
        Integer durationMinutes
) {
}
