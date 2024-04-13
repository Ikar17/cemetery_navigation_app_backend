package com.app.backend.repository;

import com.app.backend.model.Cementery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CementeryRepository extends JpaRepository<Cementery, Integer> {
}
