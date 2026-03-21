package com.antivirus.server.controller;

import com.antivirus.server.model.LicenseType;
import com.antivirus.server.repository.LicenseTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/license-types")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminLicenseTypeController {

    private final LicenseTypeRepository licenseTypeRepository;

    @PostMapping
    public ResponseEntity<?> createLicenseType(@Valid @RequestBody LicenseType licenseType) {
        try {
            if (licenseTypeRepository.findByName(licenseType.getName()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "License type with this name already exists"));
            }

            LicenseType savedType = licenseTypeRepository.save(licenseType);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "License type created successfully",
                            "licenseType", savedType
                    ));
        } catch (Exception e) {
            log.error("Error creating license type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }


    @GetMapping
    public ResponseEntity<List<LicenseType>> getAllLicenseTypes() {
        return ResponseEntity.ok(licenseTypeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getLicenseTypeById(@PathVariable Long id) {
        return licenseTypeRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "License type not found")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLicenseType(@PathVariable Long id, @Valid @RequestBody LicenseType licenseType) {
        try {
            LicenseType existingType = licenseTypeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("License type not found"));

            existingType.setName(licenseType.getName());
            existingType.setDefaultDurationInDays(licenseType.getDefaultDurationInDays());
            existingType.setDescription(licenseType.getDescription());

            LicenseType updatedType = licenseTypeRepository.save(existingType);

            return ResponseEntity.ok(Map.of(
                    "message", "License type updated successfully",
                    "licenseType", updatedType
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating license type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLicenseType(@PathVariable Long id) {
        try {
            if (!licenseTypeRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "License type not found"));
            }

            licenseTypeRepository.deleteById(id);

            return ResponseEntity.ok(Map.of("message", "License type deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting license type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
}
