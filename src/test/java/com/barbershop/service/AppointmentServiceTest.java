package com.barbershop.service;

import com.barbershop.controller.form.AppointmentForm;
import com.barbershop.model.Appointment;
import com.barbershop.model.Barber;
import com.barbershop.model.Client;
import com.barbershop.model.ServiceType;
import com.barbershop.model.ShopService;
import com.barbershop.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentServiceTest {

    @Test
    void shouldReturnExistingAppointmentWhenIdempotencyKeyAlreadyExists() {
        InMemoryAppointmentRepository repositoryHandler = new InMemoryAppointmentRepository();
        Appointment existingAppointment = appointment(10L, "existing-key");
        repositoryHandler.store(existingAppointment);

        TrackingGoogleCalendarService googleCalendarService = new TrackingGoogleCalendarService();
        TrackingClientService clientService = new TrackingClientService(client());
        TrackingBarberService barberService = new TrackingBarberService(barber());
        TrackingShopServiceService shopServiceService = new TrackingShopServiceService(shopService());

        AppointmentService appointmentService = new AppointmentService(
                repositoryHandler.proxy(),
                googleCalendarService,
                clientService,
                barberService,
                shopServiceService
        );

        AppointmentService.AppointmentCreationResult result =
                appointmentService.save(appointmentForm(), "existing-key");

        assertThat(result.created()).isFalse();
        assertThat(result.appointment()).isSameAs(existingAppointment);
        assertThat(clientService.findOrCreateCalls).isZero();
        assertThat(barberService.findByIdCalls).isZero();
        assertThat(shopServiceService.findByIdCalls).isZero();
        assertThat(googleCalendarService.createEventCalls).isZero();
    }

    @Test
    void shouldReturnPersistedAppointmentWhenConcurrentDuplicateIsDetected() {
        InMemoryAppointmentRepository repositoryHandler = new InMemoryAppointmentRepository();
        Appointment existingAppointment = appointment(20L, "race-key");
        repositoryHandler.store(existingAppointment);
        repositoryHandler.failOnNextSaveAndFlushWithDuplicate = true;

        TrackingGoogleCalendarService googleCalendarService = new TrackingGoogleCalendarService();
        AppointmentService appointmentService = new AppointmentService(
                repositoryHandler.proxy(),
                googleCalendarService,
                new TrackingClientService(client()),
                new TrackingBarberService(barber()),
                new TrackingShopServiceService(shopService())
        );

        Appointment submittedAppointment = appointment(null, "race-key");
        Appointment result = appointmentService.save(submittedAppointment);

        assertThat(result).isSameAs(existingAppointment);
        assertThat(googleCalendarService.createEventCalls).isZero();
    }

    @Test
    void shouldCreateAppointmentAndCalendarEventWhenIdempotencyKeyIsNew() {
        InMemoryAppointmentRepository repositoryHandler = new InMemoryAppointmentRepository();
        TrackingGoogleCalendarService googleCalendarService = new TrackingGoogleCalendarService();
        TrackingClientService clientService = new TrackingClientService(client());
        TrackingBarberService barberService = new TrackingBarberService(barber());
        TrackingShopServiceService shopServiceService = new TrackingShopServiceService(shopService());

        AppointmentService appointmentService = new AppointmentService(
                repositoryHandler.proxy(),
                googleCalendarService,
                clientService,
                barberService,
                shopServiceService
        );

        AppointmentService.AppointmentCreationResult result =
                appointmentService.save(appointmentForm(), "new-key");

        assertThat(result.created()).isTrue();
        assertThat(result.appointment().getId()).isNotNull();
        assertThat(result.appointment().getGoogleCalendarEventId()).isEqualTo("google-event-1");
        assertThat(result.appointment().getIdempotencyKey()).isEqualTo("new-key");
        assertThat(clientService.findOrCreateCalls).isEqualTo(1);
        assertThat(barberService.findByIdCalls).isEqualTo(1);
        assertThat(shopServiceService.findByIdCalls).isEqualTo(1);
        assertThat(googleCalendarService.createEventCalls).isEqualTo(1);
    }

    private AppointmentForm appointmentForm() {
        AppointmentForm form = new AppointmentForm();
        form.setClientName("Bruno");
        form.setBarberId(2L);
        form.setServiceId(3L);
        form.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        form.setNotes("Teste");
        form.setIdempotencyKey(UUID.randomUUID().toString());
        return form;
    }

    private Appointment appointment(Long id, String idempotencyKey) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setClient(client());
        appointment.setBarber(barber());
        appointment.setService(shopService());
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment.setNotes("Teste");
        appointment.setIdempotencyKey(idempotencyKey);
        return appointment;
    }

    private Client client() {
        Client client = new Client();
        client.setId(1L);
        client.setName("Bruno");
        client.setEmail("bruno@barbershop.local");
        client.setPhone("11999999999");
        return client;
    }

    private Barber barber() {
        Barber barber = new Barber();
        barber.setId(2L);
        barber.setName("Rafael");
        barber.setSpecialty("Fade");
        return barber;
    }

    private ShopService shopService() {
        ShopService service = new ShopService();
        service.setId(3L);
        service.setName("Corte");
        service.setType(ServiceType.CORTE);
        service.setPrice(BigDecimal.TEN);
        service.setDurationMinutes(45);
        return service;
    }

    private static final class TrackingGoogleCalendarService implements GoogleCalendarService {
        private int createEventCalls;

        @Override
        public String createEvent(Appointment appointment) {
            createEventCalls++;
            return "google-event-" + createEventCalls;
        }
    }

    private static final class TrackingClientService extends ClientService {
        private final Client client;
        private int findOrCreateCalls;

        private TrackingClientService(Client client) {
            super(null);
            this.client = client;
        }

        @Override
        public Client findOrCreateByName(String name) {
            findOrCreateCalls++;
            return client;
        }
    }

    private static final class TrackingBarberService extends BarberService {
        private final Barber barber;
        private int findByIdCalls;

        private TrackingBarberService(Barber barber) {
            super(null);
            this.barber = barber;
        }

        @Override
        public Barber findById(Long id) {
            findByIdCalls++;
            return barber;
        }
    }

    private static final class TrackingShopServiceService extends ShopServiceService {
        private final ShopService shopService;
        private int findByIdCalls;

        private TrackingShopServiceService(ShopService shopService) {
            super(null);
            this.shopService = shopService;
        }

        @Override
        public ShopService findById(Long id) {
            findByIdCalls++;
            return shopService;
        }
    }

    private static final class InMemoryAppointmentRepository implements InvocationHandler {
        private final Map<String, Appointment> appointmentsByKey = new LinkedHashMap<>();
        private long nextId = 100L;
        private boolean failOnNextSaveAndFlushWithDuplicate;

        private AppointmentRepository proxy() {
            return (AppointmentRepository) Proxy.newProxyInstance(
                    AppointmentRepository.class.getClassLoader(),
                    new Class[]{AppointmentRepository.class},
                    this
            );
        }

        private void store(Appointment appointment) {
            appointmentsByKey.put(appointment.getIdempotencyKey(), appointment);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();

            if (Objects.equals(methodName, "findByIdempotencyKey")) {
                return Optional.ofNullable(appointmentsByKey.get(args[0]));
            }
            if (Objects.equals(methodName, "saveAndFlush")) {
                Appointment appointment = (Appointment) args[0];
                if (failOnNextSaveAndFlushWithDuplicate) {
                    failOnNextSaveAndFlushWithDuplicate = false;
                    throw new DataIntegrityViolationException("duplicate key");
                }
                if (appointment.getId() == null) {
                    appointment.setId(nextId++);
                }
                appointmentsByKey.put(appointment.getIdempotencyKey(), appointment);
                return appointment;
            }
            if (Objects.equals(methodName, "save")) {
                Appointment appointment = (Appointment) args[0];
                appointmentsByKey.put(appointment.getIdempotencyKey(), appointment);
                return appointment;
            }
            if (Objects.equals(methodName, "findAll")) {
                return new ArrayList<>(appointmentsByKey.values());
            }
            if (Objects.equals(methodName, "toString")) {
                return "InMemoryAppointmentRepository";
            }
            if (Objects.equals(methodName, "hashCode")) {
                return System.identityHashCode(this);
            }
            if (Objects.equals(methodName, "equals")) {
                return proxy == args[0];
            }

            throw new UnsupportedOperationException("Metodo nao suportado no teste: " + methodName);
        }
    }
}
