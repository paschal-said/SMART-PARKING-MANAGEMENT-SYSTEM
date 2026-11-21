package com.anpr.parking.parking;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ParkingDtos {
    private ParkingDtos() {}

    public record FloorRequest(@NotBlank @Size(max = 60) String name) {}

    public record SlotRequest(@NotBlank @Size(max = 20) String slotNumber) {}

    public record SlotView(Long id, Long floorId, String slotNumber, boolean active,
                           String observedOccupancy, Instant occupancyUpdatedAt) {}

    public record FloorView(Long id, String name, List<SlotView> slots) {}
}
