package com.app.backend.repository;

import com.app.backend.model.Cemetery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CemeteryRepository extends JpaRepository<Cemetery, Integer> {
    List<Cemetery> findByCity(String city);
    Optional<Cemetery> findByAddressContainingAndCityContaining(String address, String city);
}
