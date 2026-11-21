package com.anpr.parking.parking;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    boolean existsByName(String name);
}
