package com.antivirus.server.repository;

import com.antivirus.server.model.LicenseHistory;
import com.antivirus.server.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {
    List<LicenseHistory> findByLicenseOrderByChangeDateDesc(License license);
}
