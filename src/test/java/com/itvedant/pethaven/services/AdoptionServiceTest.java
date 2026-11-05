package com.itvedant.pethaven.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.itvedant.pethaven.models.AdoptionRequest;
import com.itvedant.pethaven.models.Animal;
import com.itvedant.pethaven.models.User;
import com.itvedant.pethaven.repositories.AdoptionRequestRepository;
import com.itvedant.pethaven.repositories.AnimalRepository;
import com.itvedant.pethaven.repositories.UserRepository;

// Unit tests for the adoption rules. The repositories are mocked, so no database is needed.
@ExtendWith(MockitoExtension.class)
class AdoptionServiceTest {

    @Mock
    private AdoptionRequestRepository adoptionRequestRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdoptionService adoptionService;

    private Animal animal;
    private User user;

    @BeforeEach
    void setUp() {
        animal = new Animal();
        animal.setId(1);
        animal.setName("Bruno");

        user = new User();
        user.setId(7);
        user.setEmail("asha@example.com");
    }

    @Test
    void sendRequest_forAvailableAnimal_createsPendingRequestAndHoldsTheAnimal() {
        when(animalRepository.findById(1)).thenReturn(Optional.of(animal));
        when(userRepository.findByEmail("asha@example.com")).thenReturn(Optional.of(user));
        when(adoptionRequestRepository.save(any(AdoptionRequest.class))).thenAnswer(call -> call.getArgument(0));

        AdoptionRequest request = adoptionService.sendRequest(1, "I have a big garden", "asha@example.com");

        assertEquals("PENDING", request.getStatus());
        assertEquals("I have a big garden", request.getMessage());
        assertEquals(LocalDate.now(), request.getRequestDate());
        assertEquals(user, request.getUser());
        assertEquals("PENDING", animal.getStatus());
        verify(animalRepository).save(animal);
    }

    @Test
    void sendRequest_forAnimalThatIsNotAvailable_isRejectedWith400() {
        animal.setStatus("ADOPTED");
        when(animalRepository.findById(1)).thenReturn(Optional.of(animal));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> adoptionService.sendRequest(1, "Please", "asha@example.com"));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(adoptionRequestRepository, never()).save(any());
    }

    @Test
    void sendRequest_forUnknownAnimal_returns404() {
        when(animalRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> adoptionService.sendRequest(99, "Please", "asha@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void approveRequest_marksRequestApprovedAndAnimalAdopted() {
        AdoptionRequest request = pendingRequest();
        when(adoptionRequestRepository.findById(5)).thenReturn(Optional.of(request));
        when(adoptionRequestRepository.save(request)).thenReturn(request);

        AdoptionRequest result = adoptionService.approveRequest(5);

        assertEquals("APPROVED", result.getStatus());
        assertEquals("ADOPTED", animal.getStatus());
        verify(animalRepository).save(animal);
    }

    @Test
    void rejectRequest_marksRequestRejectedAndAnimalAvailableAgain() {
        AdoptionRequest request = pendingRequest();
        when(adoptionRequestRepository.findById(5)).thenReturn(Optional.of(request));
        when(adoptionRequestRepository.save(request)).thenReturn(request);

        AdoptionRequest result = adoptionService.rejectRequest(5);

        assertEquals("REJECTED", result.getStatus());
        assertEquals("AVAILABLE", animal.getStatus());
    }

    @Test
    void approveRequest_thatWasAlreadyDecided_isRejectedWith400() {
        AdoptionRequest request = pendingRequest();
        request.setStatus("REJECTED");
        when(adoptionRequestRepository.findById(5)).thenReturn(Optional.of(request));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> adoptionService.approveRequest(5));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(animalRepository, never()).save(any());
    }

    private AdoptionRequest pendingRequest() {
        animal.setStatus("PENDING");
        AdoptionRequest request = new AdoptionRequest();
        request.setId(5);
        request.setStatus("PENDING");
        request.setAnimal(animal);
        request.setUser(user);
        return request;
    }
}
