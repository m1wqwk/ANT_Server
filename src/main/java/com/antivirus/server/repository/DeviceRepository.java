package com.antivirus.server.repository;

import com.antivirus.server.model.Device;
import com.antivirus.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByMacAddress(String macAddress);
    List<Device> findByUser(User user);
}
