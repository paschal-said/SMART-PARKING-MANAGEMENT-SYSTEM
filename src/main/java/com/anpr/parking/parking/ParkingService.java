package com.anpr.parking.parking;

import com.anpr.parking.common.ApiException;
import com.anpr.parking.common.AuditService;
import com.anpr.parking.parking.ParkingDtos.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ParkingService {
    private final FloorRepository floors;
    private final SlotRepository slots;
    private final AuditService audit;
    private final Duration staleAfter;

    public ParkingService(FloorRepository floors, SlotRepository slots, AuditService audit,
                          @Value("${app.sensor.stale-seconds:60}") long staleSeconds) {
        this.floors = floors;
        this.slots = slots;
        this.audit = audit;
        this.staleAfter = Duration.ofSeconds(staleSeconds);
    }

    public List<FloorView> listFloors() {
        return floors.findAll().stream()
                .map(f -> new FloorView(f.getId(), f.getName(),
                        slots.findByFloorIdOrderBySlotNumber(f.getId()).stream().map(this::view).toList()))
                .toList();
    }

    public FloorView createFloor(Long adminId, FloorRequest r) {
        String name = r.name().trim();
        if (floors.existsByName(name)) throw ApiException.conflict("Floor name already exists");
        Floor f = new Floor();
        f.setName(name);
        f = floors.save(f);
        audit.log("FLOOR_CREATED", adminId, "FLOOR", f.getId(), "OK", name);
        return new FloorView(f.getId(), f.getName(), List.of());
    }

    public FloorView renameFloor(Long adminId, Long id, FloorRequest r) {
        Floor f = floors.findById(id).orElseThrow(() -> ApiException.notFound("Floor not found"));
        String name = r.name().trim();
        if (!name.equals(f.getName()) && floors.existsByName(name)) {
            throw ApiException.conflict("Floor name already exists");
        }
        f.setName(name);
        floors.save(f);
        audit.log("FLOOR_RENAMED", adminId, "FLOOR", id, "OK", name);
        return new FloorView(f.getId(), f.getName(),
                slots.findByFloorIdOrderBySlotNumber(id).stream().map(this::view).toList());
    }

    public SlotView createSlot(Long adminId, Long floorId, SlotRequest r) {
        floors.findById(floorId).orElseThrow(() -> ApiException.notFound("Floor not found"));
        String number = r.slotNumber().trim().toUpperCase();
        if (slots.existsByFloorIdAndSlotNumber(floorId, number)) {
            throw ApiException.conflict("Slot number already exists on this floor");
        }
        ParkingSlot s = new ParkingSlot();
        s.setFloorId(floorId);
        s.setSlotNumber(number);
        s = slots.save(s);
        audit.log("SLOT_CREATED", adminId, "SLOT", s.getId(), "OK", number);
        return view(s);
    }

    public SlotView setSlotActive(Long adminId, Long slotId, boolean active) {
        ParkingSlot s = slots.findById(slotId).orElseThrow(() -> ApiException.notFound("Slot not found"));
        s.setActive(active);
        slots.save(s);
        audit.log(active ? "SLOT_ACTIVATED" : "SLOT_DEACTIVATED", adminId, "SLOT", slotId, "OK", null);
        return view(s);
    }

    SlotView view(ParkingSlot s) {
        boolean fresh = s.getOccupancyUpdatedAt() != null
                && Duration.between(s.getOccupancyUpdatedAt(), Instant.now()).compareTo(staleAfter) <= 0;
        String observed = fresh ? s.getOccupancyStatus() : "UNKNOWN";
        return new SlotView(s.getId(), s.getFloorId(), s.getSlotNumber(), s.isActive(),
                observed, s.getOccupancyUpdatedAt());
    }
}
