package com.barbershop.service;

import com.barbershop.model.Barber;
import com.barbershop.repository.BarberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarberService {

    private final BarberRepository barberRepository;

    public BarberService(BarberRepository barberRepository) {
        this.barberRepository = barberRepository;
    }

    public List<Barber> findAll() {
        return barberRepository.findAll();
    }

    public Barber save(Barber barber) {
        return barberRepository.save(barber);
    }

    public Barber findById(Long id) {
        return barberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro nao encontrado"));
    }
}
