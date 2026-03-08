package com.barbershop.service;

import com.barbershop.controller.form.AppointmentForm;
import com.barbershop.model.Appointment;
import com.barbershop.repository.AppointmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final GoogleCalendarService googleCalendarService;
    private final ClientService clientService;
    private final BarberService barberService;
    private final ShopServiceService shopServiceService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              GoogleCalendarService googleCalendarService,
                              ClientService clientService,
                              BarberService barberService,
                              ShopServiceService shopServiceService) {
        this.appointmentRepository = appointmentRepository;
        this.googleCalendarService = googleCalendarService;
        this.clientService = clientService;
        this.barberService = barberService;
        this.shopServiceService = shopServiceService;
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    @Transactional
    public Appointment save(Appointment appointment) {
        return saveInternal(appointment);
    }

    @Transactional
    public AppointmentCreationResult save(AppointmentForm form, String idempotencyKey) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        Optional<Appointment> existingAppointment = appointmentRepository.findByIdempotencyKey(normalizedKey);
        if (existingAppointment.isPresent()) {
            return new AppointmentCreationResult(existingAppointment.get(), false);
        }

        Appointment appointment = new Appointment();
        appointment.setClient(clientService.findOrCreateByName(form.getClientName()));
        appointment.setBarber(barberService.findById(form.getBarberId()));
        appointment.setService(shopServiceService.findById(form.getServiceId()));
        appointment.setAppointmentDateTime(form.getAppointmentDateTime());
        appointment.setNotes(form.getNotes());
        appointment.setIdempotencyKey(normalizedKey);

        return new AppointmentCreationResult(saveInternal(appointment), true);
    }

    @Transactional
    public Appointment save(AppointmentForm form) {
        return save(form, form.getIdempotencyKey()).appointment();
    }

    public String newIdempotencyKey() {
        return UUID.randomUUID().toString();
    }

    private Appointment saveInternal(Appointment appointment) {
        appointment.setIdempotencyKey(normalizeIdempotencyKey(appointment.getIdempotencyKey()));
        PersistedAppointment persistedAppointment = persistOrLoadExisting(appointment);
        if (!persistedAppointment.created()) {
            return persistedAppointment.appointment();
        }

        String eventId = googleCalendarService.createEvent(persistedAppointment.appointment());
        persistedAppointment.appointment().setGoogleCalendarEventId(eventId);
        return appointmentRepository.save(persistedAppointment.appointment());
    }

    private PersistedAppointment persistOrLoadExisting(Appointment appointment) {
        try {
            return new PersistedAppointment(appointmentRepository.saveAndFlush(appointment), true);
        } catch (DataIntegrityViolationException exception) {
            Appointment existingAppointment = appointmentRepository.findByIdempotencyKey(appointment.getIdempotencyKey())
                    .orElseThrow(() -> exception);
            return new PersistedAppointment(existingAppointment, false);
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        String normalizedKey = idempotencyKey == null ? "" : idempotencyKey.trim();
        if (normalizedKey.isBlank()) {
            throw new BadRequestException("Idempotency key e obrigatoria");
        }
        return normalizedKey;
    }

    public record AppointmentCreationResult(Appointment appointment, boolean created) {
    }

    private record PersistedAppointment(Appointment appointment, boolean created) {
    }
}
