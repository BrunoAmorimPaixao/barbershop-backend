package com.barbershop.repository;

import com.barbershop.model.ShopService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopServiceRepository extends JpaRepository<ShopService, Long> {
}
