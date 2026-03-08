package com.barbershop.api;

import com.barbershop.api.mapper.ApiMapper;
import com.barbershop.api.request.AppointmentRequest;
import com.barbershop.api.response.AppointmentResponse;
import com.barbershop.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentApiController {

    private final AppointmentService appointmentService;

    public AppointmentApiController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponse> findAll() {
        return appointmentService.findAll().stream()
                .map(ApiMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request,
                                                      @RequestHeader("Idempotency-Key") String idempotencyKey) {
        AppointmentService.AppointmentCreationResult result =
                appointmentService.save(ApiMapper.toForm(request), idempotencyKey);

        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiMapper.toResponse(result.appointment()));
    }
}
