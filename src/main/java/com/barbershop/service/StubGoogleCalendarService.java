package com.barbershop.service;

import com.barbershop.model.Appointment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.google.calendar", name = "enabled", havingValue = "false", matchIfMissing = true)
public class StubGoogleCalendarService implements GoogleCalendarService {

    @Override
    public String createEvent(Appointment appointment) {
        return "PENDENTE_CONFIGURACAO_GOOGLE";
    }
}
