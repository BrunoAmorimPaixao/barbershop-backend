package com.barbershop.service;

import com.barbershop.api.UnauthorizedException;
import com.barbershop.api.request.LoginRequest;
import com.barbershop.api.response.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AuthService {

    private final String adminEmail;
    private final String adminPassword;
    private final String displayName;

    public AuthService(@Value("${app.auth.email:admin@barbershop.com}") String adminEmail,
                       @Value("${app.auth.password:admin123}") String adminPassword,
                       @Value("${app.auth.display-name:Administrador}") String displayName) {
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.displayName = displayName;
    }

    public LoginResponse login(LoginRequest request) {
        if (!adminEmail.equalsIgnoreCase(request.email()) || !adminPassword.equals(request.password())) {
            throw new UnauthorizedException("E-mail ou senha invalidos");
        }

        String tokenSource = request.email() + ":" + request.password();
        String token = Base64.getEncoder().encodeToString(tokenSource.getBytes(StandardCharsets.UTF_8));
        return new LoginResponse(request.email(), displayName, token);
    }
}
