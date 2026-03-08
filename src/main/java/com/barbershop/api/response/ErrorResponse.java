package com.barbershop.api.response;

import java.util.List;

public record ErrorResponse(
        String message,
        List<String> details
) {
}
