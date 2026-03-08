package com.barbershop.service;

import com.barbershop.model.Appointment;

public interface GoogleCalendarService {

    String createEvent(Appointment appointment);
}
