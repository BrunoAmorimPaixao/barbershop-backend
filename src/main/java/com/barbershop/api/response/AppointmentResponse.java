package com.barbershop.api.response;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long clientId,
        String clientName,
        Long barberId,
        String barberName,
        Long serviceId,
        String serviceName,
        LocalDateTime appointmentDateTime,
        String notes,
        String googleCalendarEventId
) {
}
