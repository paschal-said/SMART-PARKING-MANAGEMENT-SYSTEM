package com.anpr.parking.parking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotRepository extends JpaRepository<ParkingSlot, Long> {
    List<ParkingSlot> findByFloorIdOrderBySlotNumber(Long floorId);
    boolean existsByFloorIdAndSlotNumber(Long floorId, String slotNumber);
}
