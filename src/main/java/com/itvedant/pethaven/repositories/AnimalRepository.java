package com.itvedant.pethaven.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itvedant.pethaven.models.Animal;

public interface AnimalRepository extends JpaRepository<Animal, Integer> {

}