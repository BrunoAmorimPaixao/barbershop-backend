package com.barbershop.api;

import com.barbershop.api.mapper.ApiMapper;
import com.barbershop.api.request.BarberRequest;
import com.barbershop.api.response.BarberResponse;
import com.barbershop.service.BarberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/barbers")
public class BarberApiController {

    private final BarberService barberService;

    public BarberApiController(BarberService barberService) {
        this.barberService = barberService;
    }

    @GetMapping
    public List<BarberResponse> findAll() {
        return barberService.findAll().stream()
                .map(ApiMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BarberResponse create(@Valid @RequestBody BarberRequest request) {
        return ApiMapper.toResponse(barberService.save(ApiMapper.toEntity(request)));
    }
}
