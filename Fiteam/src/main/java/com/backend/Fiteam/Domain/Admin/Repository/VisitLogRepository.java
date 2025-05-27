package com.backend.Fiteam.Domain.Admin.Repository;

import com.backend.Fiteam.Domain.Admin.Entity.VisitLog;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {
    long countDistinctUserIdByVisitDate(LocalDate visitDate);
}

