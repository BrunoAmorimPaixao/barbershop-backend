package com.barbershop.service;

import com.barbershop.model.ShopService;
import com.barbershop.repository.ShopServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopServiceService {

    private final ShopServiceRepository shopServiceRepository;

    public ShopServiceService(ShopServiceRepository shopServiceRepository) {
        this.shopServiceRepository = shopServiceRepository;
    }

    public List<ShopService> findAll() {
        return shopServiceRepository.findAll();
    }

    public ShopService save(ShopService shopService) {
        return shopServiceRepository.save(shopService);
    }

    public ShopService findById(Long id) {
        return shopServiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servico nao encontrado"));
    }
}
