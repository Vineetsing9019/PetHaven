package com.itvedant.pethaven.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itvedant.pethaven.models.AdoptionRequest;
import com.itvedant.pethaven.services.AdoptionService;

@RestController
@RequestMapping("/adoptions")
public class AdoptionController {

    @Autowired
    private AdoptionService adoptionService;

    // USER: send adoption request for an animal
    @PostMapping("/request/{animalId}")
    public ResponseEntity<AdoptionRequest> sendRequest(@PathVariable Integer animalId,
                                                       @RequestBody AdoptionRequest body,
                                                       Principal principal) {
        return ResponseEntity.ok(adoptionService.sendRequest(animalId, body.getMessage(), principal.getName()));
    }

    // USER: view my requests
    @GetMapping("/my")
    public ResponseEntity<List<AdoptionRequest>> myRequests(Principal principal) {
        return ResponseEntity.ok(adoptionService.getMyRequests(principal.getName()));
    }

    // ADMIN: view all requests
    @GetMapping("/all")
    public ResponseEntity<List<AdoptionRequest>> allRequests() {
        return ResponseEntity.ok(adoptionService.getAllRequests());
    }

    // ADMIN: approve request
    @PutMapping("/{id}/approve")
    public ResponseEntity<AdoptionRequest> approve(@PathVariable Integer id) {
        return ResponseEntity.ok(adoptionService.approveRequest(id));
    }

    // ADMIN: reject request
    @PutMapping("/{id}/reject")
    public ResponseEntity<AdoptionRequest> reject(@PathVariable Integer id) {
        return ResponseEntity.ok(adoptionService.rejectRequest(id));
    }
}