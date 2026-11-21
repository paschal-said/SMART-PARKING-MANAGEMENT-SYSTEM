package com.anpr.parking.vehicle;

import com.anpr.parking.config.AuthUser;
import com.anpr.parking.user.UserDtos.ActiveRequest;
import com.anpr.parking.vehicle.VehicleDtos.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class VehicleController {
    private final VehicleService service;

    public VehicleController(VehicleService service) { this.service = service; }

    @PostMapping("/api/vehicles")
    @PreAuthorize("hasRole('DRIVER')")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleView create(@Valid @RequestBody CreateVehicleRequest r, @AuthenticationPrincipal AuthUser me) {
        return service.create(me.id(), r);
    }

    @GetMapping("/api/vehicles")
    @PreAuthorize("hasRole('DRIVER')")
    public List<VehicleView> mine(@AuthenticationPrincipal AuthUser me) {
        return service.listMine(me.id());
    }

    @PutMapping("/api/vehicles/{id}")
    @PreAuthorize("hasRole('DRIVER')")
    public VehicleView update(@PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest r,
                              @AuthenticationPrincipal AuthUser me) {
        return service.update(me.id(), id, r);
    }

    @DeleteMapping("/api/vehicles/{id}")
    @PreAuthorize("hasRole('DRIVER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id, @AuthenticationPrincipal AuthUser me) {
        service.remove(me.id(), id);
    }

    @GetMapping("/api/admin/vehicles")
    public List<VehicleView> all() { return service.listAll(); }

    @PatchMapping("/api/admin/vehicles/{id}/active")
    public VehicleView adminActive(@PathVariable Long id, @Valid @RequestBody ActiveRequest r,
                                   @AuthenticationPrincipal AuthUser admin) {
        return service.adminSetActive(admin.id(), id, r.active());
    }
}
