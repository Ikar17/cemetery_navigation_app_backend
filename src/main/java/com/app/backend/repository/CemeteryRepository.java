package com.app.backend.repository;

import com.app.backend.model.Cemetery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CemeteryRepository extends JpaRepository<Cemetery, Integer> {
}
