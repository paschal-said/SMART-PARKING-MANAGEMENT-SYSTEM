package com.anpr.parking.vehicle;

import jakarta.validation.constraints.*;
import java.time.Instant;

public final class VehicleDtos {
    private VehicleDtos() {}

    public record CreateVehicleRequest(
            @NotBlank @Size(max = 20) String plateNumber,
            @NotBlank @Size(max = 40) String vehicleType,
            @Size(max = 80) String vehicleModel) {}

    public record UpdateVehicleRequest(
            @Size(max = 20) String plateNumber,
            @Size(max = 40) String vehicleType,
            @Size(max = 80) String vehicleModel) {}

    public record VehicleView(Long id, Long userId, String plateNumber, String vehicleType,
                              String vehicleModel, boolean active, Instant createdAt) {
        public static VehicleView of(Vehicle v) {
            return new VehicleView(v.getId(), v.getUserId(), v.getPlateNumber(), v.getVehicleType(),
                    v.getVehicleModel(), v.isActive(), v.getCreatedAt());
        }
    }
}
