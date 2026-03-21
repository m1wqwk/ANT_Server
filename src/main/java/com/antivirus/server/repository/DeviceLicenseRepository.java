package com.antivirus.server.repository;

import com.antivirus.server.model.DeviceLicense;
import com.antivirus.server.model.License;
import com.antivirus.server.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {
    Optional<DeviceLicense> findByLicenseAndDevice(License license, Device device);
    long countByLicense(License license);
}
