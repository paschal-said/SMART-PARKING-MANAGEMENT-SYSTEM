package com.anpr.parking.parking;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.anpr.parking.config.AuthUser;
import com.anpr.parking.parking.ParkingDtos.FloorRequest;
import com.anpr.parking.parking.ParkingDtos.FloorView;
import com.anpr.parking.parking.ParkingDtos.SlotRequest;
import com.anpr.parking.parking.ParkingDtos.SlotView;
import com.anpr.parking.user.UserDtos.ActiveRequest;

import jakarta.validation.Valid;

@RestController
public class ParkingController {
    private final ParkingService service;

    public ParkingController(ParkingService service) { this.service = service; }

    @GetMapping("/api/floors")
    public List<FloorView> floors() { return service.listFloors(); }

    @PostMapping("/api/admin/floors")
    @ResponseStatus(HttpStatus.CREATED)
    public FloorView createFloor(@Valid @RequestBody FloorRequest r, @AuthenticationPrincipal AuthUser admin) {
        return service.createFloor(admin.id(), r);
    }

    @PutMapping("/api/admin/floors/{id}")
    public FloorView renameFloor(@PathVariable Long id, @Valid @RequestBody FloorRequest r,
                                 @AuthenticationPrincipal AuthUser admin) {
        return service.renameFloor(admin.id(), id, r);
    }

    @PostMapping("/api/admin/floors/{floorId}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotView createSlot(@PathVariable Long floorId, @Valid @RequestBody SlotRequest r,
                               @AuthenticationPrincipal AuthUser admin) {
        return service.createSlot(admin.id(), floorId, r);
    }

    @PatchMapping("/api/admin/slots/{id}/active")
    public SlotView slotActive(@PathVariable Long id, @Valid @RequestBody ActiveRequest r,
                               @AuthenticationPrincipal AuthUser admin) {
        return service.setSlotActive(admin.id(), id, r.active());
    }
}
