package com.app.backend.repository;

import com.app.backend.model.Decedent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecedentRepository extends JpaRepository<Decedent, Integer> {
    List<Decedent> findBySurname(String surname);
    List<Decedent> findByNameContainingIgnoreCaseAndSurnameContainingIgnoreCase(String name, String surname);
}