package com.anpr.parking.parking;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long id;

    @Column(name = "floor_id", nullable = false)
    private Long floorId;

    @Column(name = "slot_number", nullable = false)
    private String slotNumber;

    @Column(name = "occupancy_status", nullable = false)
    private String occupancyStatus = "UNKNOWN";

    @Column(name = "occupancy_updated_at")
    private Instant occupancyUpdatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public Long getFloorId() { return floorId; }
    public void setFloorId(Long v) { this.floorId = v; }
    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String v) { this.slotNumber = v; }
    public String getOccupancyStatus() { return occupancyStatus; }
    public void setOccupancyStatus(String v) { this.occupancyStatus = v; }
    public Instant getOccupancyUpdatedAt() { return occupancyUpdatedAt; }
    public void setOccupancyUpdatedAt(Instant v) { this.occupancyUpdatedAt = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
}
