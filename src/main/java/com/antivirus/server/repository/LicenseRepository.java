package com.antivirus.server.repository;

import com.antivirus.server.model.License;
import com.antivirus.server.model.Product;
import com.antivirus.server.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByCode(String code);

    @Query("SELECT l FROM License l WHERE l.user = :user AND l.product = :product " +
            "AND l.blocked = false AND l.endingDate >= :currentDate " +
            "AND EXISTS (SELECT dl FROM DeviceLicense dl WHERE dl.license = l AND dl.device.macAddress = :macAddress)")
    Optional<License> findActiveByDeviceUserAndProduct(
            @Param("user") User user,
            @Param("product") Product product,
            @Param("macAddress") String macAddress,
            @Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(dl) FROM DeviceLicense dl WHERE dl.license = :license")
    long countActiveDevicesByLicense(@Param("license") License license);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM License l WHERE l.code = :code")
    Optional<License> findByCodeForUpdate(@Param("code") String code);
    List<License> findByUser(User user);
}