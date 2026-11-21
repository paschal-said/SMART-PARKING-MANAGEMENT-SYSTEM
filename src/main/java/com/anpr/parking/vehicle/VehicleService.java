package com.anpr.parking.vehicle;

import com.anpr.parking.common.ApiException;
import com.anpr.parking.common.AuditService;
import com.anpr.parking.common.PlateUtil;
import com.anpr.parking.vehicle.VehicleDtos.*;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    private final VehicleRepository vehicles;
    private final AuditService audit;

    public VehicleService(VehicleRepository vehicles, AuditService audit) {
        this.vehicles = vehicles;
        this.audit = audit;
    }

    public VehicleView create(Long userId, CreateVehicleRequest r) {
        String plate = checkedPlate(r.plateNumber());
        if (vehicles.existsByPlateNumberAndActiveTrue(plate)) {
            throw ApiException.conflict("This plate number is already registered");
        }
        Vehicle v = new Vehicle();
        v.setUserId(userId);
        v.setPlateNumber(plate);
        v.setVehicleType(r.vehicleType().trim());
        v.setVehicleModel(r.vehicleModel());
        return VehicleView.of(vehicles.save(v));
    }

    public List<VehicleView> listMine(Long userId) {
        return vehicles.findByUserIdAndActiveTrueOrderByIdDesc(userId).stream().map(VehicleView::of).toList();
    }

    public VehicleView update(Long userId, Long id, UpdateVehicleRequest r) {
        Vehicle v = ownedActive(userId, id);
        if (r.plateNumber() != null && !r.plateNumber().isBlank()) {
            String plate = checkedPlate(r.plateNumber());
            if (!plate.equals(v.getPlateNumber())) {
                if (vehicles.existsByPlateNumberAndActiveTrue(plate)) {
                    throw ApiException.conflict("This plate number is already registered");
                }
                v.setPlateNumber(plate);
            }
        }
        if (r.vehicleType() != null && !r.vehicleType().isBlank()) v.setVehicleType(r.vehicleType().trim());
        if (r.vehicleModel() != null) v.setVehicleModel(r.vehicleModel());
        return VehicleView.of(vehicles.save(v));
    }

    public void remove(Long userId, Long id) {
        Vehicle v = ownedActive(userId, id);
        v.setActive(false);
        vehicles.save(v);
    }

    public List<VehicleView> listAll() {
        return vehicles.findAll().stream().map(VehicleView::of).toList();
    }

    public VehicleView adminSetActive(Long adminId, Long id, boolean active) {
        Vehicle v = vehicles.findById(id).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        if (active && !v.isActive() && vehicles.existsByPlateNumberAndActiveTrue(v.getPlateNumber())) {
            throw ApiException.conflict("Another active vehicle already uses this plate");
        }
        v.setActive(active);
        vehicles.save(v);
        audit.log(active ? "VEHICLE_ACTIVATED" : "VEHICLE_DEACTIVATED", adminId, "VEHICLE", id, "OK", null);
        return VehicleView.of(v);
    }

    private Vehicle ownedActive(Long userId, Long id) {
        Vehicle v = vehicles.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        if (!v.isActive()) throw ApiException.notFound("Vehicle not found");
        return v;
    }

    private String checkedPlate(String raw) {
        String plate = PlateUtil.normalize(raw);
        if (!PlateUtil.isValid(plate)) {
            throw ApiException.badRequest("Invalid plate number (4-12 letters/digits expected)");
        }
        return plate;
    }
}