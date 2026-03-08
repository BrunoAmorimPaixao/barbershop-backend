package com.barbershop.api;

import com.barbershop.api.mapper.ApiMapper;
import com.barbershop.api.request.ShopServiceRequest;
import com.barbershop.api.response.ShopServiceResponse;
import com.barbershop.service.ShopServiceService;
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
@RequestMapping("/api/services")
public class ShopServiceApiController {

    private final ShopServiceService shopServiceService;

    public ShopServiceApiController(ShopServiceService shopServiceService) {
        this.shopServiceService = shopServiceService;
    }

    @GetMapping
    public List<ShopServiceResponse> findAll() {
        return shopServiceService.findAll().stream()
                .map(ApiMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShopServiceResponse create(@Valid @RequestBody ShopServiceRequest request) {
        return ApiMapper.toResponse(shopServiceService.save(ApiMapper.toEntity(request)));
    }
}
