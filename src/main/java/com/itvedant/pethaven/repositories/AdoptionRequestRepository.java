package com.itvedant.pethaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.itvedant.pethaven.models.AdoptionRequest;
import com.itvedant.pethaven.models.User;

@RepositoryRestResource(exported = false)
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Integer> {

    List<AdoptionRequest> findByUser(User user);
}