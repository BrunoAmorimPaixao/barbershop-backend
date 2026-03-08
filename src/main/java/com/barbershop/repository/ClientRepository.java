package com.barbershop.repository;

import com.barbershop.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findFirstByNameIgnoreCase(String name);
}
