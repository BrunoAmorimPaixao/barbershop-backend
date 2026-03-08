package com.barbershop.api.mapper;

import com.barbershop.api.request.AppointmentRequest;
import com.barbershop.api.request.BarberRequest;
import com.barbershop.api.request.ClientRequest;
import com.barbershop.api.request.ShopServiceRequest;
import com.barbershop.api.response.AppointmentResponse;
import com.barbershop.api.response.BarberResponse;
import com.barbershop.api.response.ClientResponse;
import com.barbershop.api.response.ShopServiceResponse;
import com.barbershop.controller.form.AppointmentForm;
import com.barbershop.model.Appointment;
import com.barbershop.model.Barber;
import com.barbershop.model.Client;
import com.barbershop.model.ShopService;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static Client toEntity(ClientRequest request) {
        Client client = new Client();
        client.setName(request.name());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        return client;
    }

    public static Barber toEntity(BarberRequest request) {
        Barber barber = new Barber();
        barber.setName(request.name());
        barber.setSpecialty(request.specialty());
        return barber;
    }

    public static ShopService toEntity(ShopServiceRequest request) {
        ShopService service = new ShopService();
        service.setName(request.name());
        service.setType(request.type());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        return service;
    }

    public static AppointmentForm toForm(AppointmentRequest request) {
        AppointmentForm form = new AppointmentForm();
        form.setClientName(request.clientName());
        form.setBarberId(request.barberId());
        form.setServiceId(request.serviceId());
        form.setAppointmentDateTime(request.appointmentDateTime());
        form.setNotes(request.notes());
        return form;
    }

    public static ClientResponse toResponse(Client client) {
        return new ClientResponse(client.getId(), client.getName(), client.getEmail(), client.getPhone());
    }

    public static BarberResponse toResponse(Barber barber) {
        return new BarberResponse(barber.getId(), barber.getName(), barber.getSpecialty());
    }

    public static ShopServiceResponse toResponse(ShopService service) {
        return new ShopServiceResponse(
                service.getId(),
                service.getName(),
                service.getType(),
                service.getPrice(),
                service.getDurationMinutes()
        );
    }

    public static AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getClient().getId(),
                appointment.getClient().getName(),
                appointment.getBarber().getId(),
                appointment.getBarber().getName(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getAppointmentDateTime(),
                appointment.getNotes(),
                appointment.getGoogleCalendarEventId()
        );
    }
}
