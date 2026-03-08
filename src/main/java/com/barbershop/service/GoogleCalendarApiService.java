package com.barbershop.service;

import com.barbershop.model.Appointment;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "app.google.calendar", name = "enabled", havingValue = "true")
public class GoogleCalendarApiService implements GoogleCalendarService {

    private final String calendarId;
    private final Resource credentialsResource;
    private final RestClient restClient;

    public GoogleCalendarApiService(
            @Value("${app.google.calendar.id}") String calendarId,
            @Value("${app.google.calendar.credentials-location}") Resource credentialsResource) {
        this.calendarId = calendarId;
        this.credentialsResource = credentialsResource;
        this.restClient = RestClient.builder()
                .baseUrl("https://www.googleapis.com/calendar/v3")
                .build();
    }

    @Override
    public String createEvent(Appointment appointment) {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            OffsetDateTime start = appointment.getAppointmentDateTime().atZone(zoneId).toOffsetDateTime();
            OffsetDateTime end = appointment.getAppointmentDateTime()
                    .plusMinutes(appointment.getService().getDurationMinutes())
                    .atZone(zoneId)
                    .toOffsetDateTime();

            Map<String, Object> payload = Map.of(
                    "summary", "Agendamento - " + appointment.getService().getName(),
                    "description", buildDescription(appointment),
                    "start", Map.of("dateTime", start.toString(), "timeZone", zoneId.getId()),
                    "end", Map.of("dateTime", end.toString(), "timeZone", zoneId.getId()));

            Map<?, ?> response = restClient.post()
                    .uri("/calendars/{calendarId}/events", calendarId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + fetchAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("id") == null) {
                throw new IllegalStateException("Google Calendar nao retornou o identificador do evento");
            }
            return response.get("id").toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Falha ao salvar agendamento no Google Calendar", exception);
        }
    }

    private String fetchAccessToken() throws IOException {
        try (InputStream inputStream = credentialsResource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/calendar"));
            credentials.refreshIfExpired();
            if (credentials.getAccessToken() == null) {
                credentials.refresh();
            }
            return credentials.getAccessToken().getTokenValue();
        }
    }

    private String buildDescription(Appointment appointment) {
        return "Cliente: " + appointment.getClient().getName()
                + "\nBarbeiro: " + appointment.getBarber().getName()
                + "\nServico: " + appointment.getService().getName()
                + "\nObservacoes: " + (appointment.getNotes() == null ? "" : appointment.getNotes());
    }
}
