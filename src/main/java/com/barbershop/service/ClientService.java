package com.barbershop.service;

import com.barbershop.model.Client;
import com.barbershop.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado"));
    }

    public Client findOrCreateByName(String name) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente e obrigatorio");
        }

        return clientRepository.findFirstByNameIgnoreCase(normalizedName)
                .orElseGet(() -> createQuickClient(normalizedName));
    }

    private Client createQuickClient(String name) {
        Client client = new Client();
        client.setName(name);
        client.setPhone("Nao informado");
        client.setEmail(generatePlaceholderEmail(name));
        return clientRepository.save(client);
    }

    private String generatePlaceholderEmail(String name) {
        String slug = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("(^\\.|\\.$)", "");
        if (slug.isBlank()) {
            slug = "cliente";
        }
        return slug + "." + UUID.randomUUID().toString().substring(0, 8) + "@barbershop.local";
    }
}
