package com.itvedant.pethaven.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.itvedant.pethaven.models.AdoptionRequest;
import com.itvedant.pethaven.models.Animal;
import com.itvedant.pethaven.models.User;
import com.itvedant.pethaven.repositories.AdoptionRequestRepository;
import com.itvedant.pethaven.repositories.AnimalRepository;
import com.itvedant.pethaven.repositories.UserRepository;

@Service
public class AdoptionService {

    @Autowired
    private AdoptionRequestRepository adoptionRequestRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private UserRepository userRepository;

    public AdoptionRequest sendRequest(Integer animalId, String message, String email) {

        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found"));

        if (!"AVAILABLE".equals(animal.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Animal is not available for adoption");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        AdoptionRequest request = new AdoptionRequest();
        request.setRequestDate(LocalDate.now());
        request.setMessage(message);
        request.setStatus("PENDING");
        request.setUser(user);
        request.setAnimal(animal);

        animal.setStatus("PENDING");
        animalRepository.save(animal);

        return adoptionRequestRepository.save(request);
    }

    public List<AdoptionRequest> getMyRequests(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return adoptionRequestRepository.findByUser(user);
    }

    public List<AdoptionRequest> getAllRequests() {
        return adoptionRequestRepository.findAll();
    }

    public AdoptionRequest approveRequest(Integer requestId) {

        AdoptionRequest request = getPendingRequest(requestId);

        request.setStatus("APPROVED");

        Animal animal = request.getAnimal();
        animal.setStatus("ADOPTED");
        animalRepository.save(animal);

        return adoptionRequestRepository.save(request);
    }

    public AdoptionRequest rejectRequest(Integer requestId) {

        AdoptionRequest request = getPendingRequest(requestId);

        request.setStatus("REJECTED");

        Animal animal = request.getAnimal();
        animal.setStatus("AVAILABLE");
        animalRepository.save(animal);

        return adoptionRequestRepository.save(request);
    }

    private AdoptionRequest getPendingRequest(Integer requestId) {

        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is already " + request.getStatus());
        }

        return request;
    }
}