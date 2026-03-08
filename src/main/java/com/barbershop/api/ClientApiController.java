package com.barbershop.api;

import com.barbershop.api.mapper.ApiMapper;
import com.barbershop.api.request.ClientRequest;
import com.barbershop.api.response.ClientResponse;
import com.barbershop.service.ClientService;
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
@RequestMapping("/api/clients")
public class ClientApiController {

    private final ClientService clientService;

    public ClientApiController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientResponse> findAll() {
        return clientService.findAll().stream()
                .map(ApiMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@Valid @RequestBody ClientRequest request) {
        return ApiMapper.toResponse(clientService.save(ApiMapper.toEntity(request)));
    }
}
