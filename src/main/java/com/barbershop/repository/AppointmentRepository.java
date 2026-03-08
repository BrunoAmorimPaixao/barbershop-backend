package com.barbershop.repository;

import com.barbershop.model.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Override
    @EntityGraph(attributePaths = {"client", "barber", "service"})
    List<Appointment> findAll();

    @EntityGraph(attributePaths = {"client", "barber", "service"})
    Optional<Appointment> findByIdempotencyKey(String idempotencyKey);
}
