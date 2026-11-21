package com.anpr.parking.vehicle;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String v) { this.plateNumber = v; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String v) { this.vehicleType = v; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String v) { this.vehicleModel = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Instant getCreatedAt() { return createdAt; }
}
