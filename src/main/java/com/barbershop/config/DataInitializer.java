package com.barbershop.config;

import com.barbershop.model.Barber;
import com.barbershop.model.ServiceType;
import com.barbershop.model.ShopService;
import com.barbershop.repository.BarberRepository;
import com.barbershop.repository.ShopServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(BarberRepository barberRepository, ShopServiceRepository shopServiceRepository) {
        return args -> {
            if (barberRepository.count() == 0) {
                Barber barber = new Barber();
                barber.setName("Barbeiro Principal");
                barber.setSpecialty("Cortes classicos e degradê");
                barberRepository.save(barber);
            }

            if (shopServiceRepository.count() == 0) {
                ShopService corte = new ShopService();
                corte.setName("Corte");
                corte.setType(ServiceType.CORTE);
                corte.setPrice(new BigDecimal("35.00"));
                corte.setDurationMinutes(45);

                ShopService barba = new ShopService();
                barba.setName("Barba");
                barba.setType(ServiceType.BARBA);
                barba.setPrice(new BigDecimal("25.00"));
                barba.setDurationMinutes(30);

                ShopService combo = new ShopService();
                combo.setName("Corte e Barba");
                combo.setType(ServiceType.CORTE_E_BARBA);
                combo.setPrice(new BigDecimal("55.00"));
                combo.setDurationMinutes(75);

                shopServiceRepository.save(corte);
                shopServiceRepository.save(barba);
                shopServiceRepository.save(combo);
            }
        };
    }
}
