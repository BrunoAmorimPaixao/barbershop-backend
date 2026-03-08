package com.barbershop.api.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotBlank String clientName,
        @NotNull Long barberId,
        @NotNull Long serviceId,
        @NotNull @Future
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime appointmentDateTime,
        String notes
) {
}
