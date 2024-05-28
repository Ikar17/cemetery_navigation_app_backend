package com.app.backend.repository;

import com.app.backend.model.Decedent;
import com.app.backend.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {
    Long countByDecedent(Decedent decedent);
    List<Route> findByDecedent_Id(Integer decedentId);
}
