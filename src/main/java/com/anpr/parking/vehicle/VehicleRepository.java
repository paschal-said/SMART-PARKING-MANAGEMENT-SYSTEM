package com.anpr.parking.vehicle;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByUserIdAndActiveTrueOrderByIdDesc(Long userId);
    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);
    Optional<Vehicle> findByPlateNumberAndActiveTrue(String plateNumber);
    boolean existsByPlateNumberAndActiveTrue(String plateNumber);
}