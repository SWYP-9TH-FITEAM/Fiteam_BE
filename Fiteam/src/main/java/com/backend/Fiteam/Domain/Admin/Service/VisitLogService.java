// src/main/java/com/backend/Fiteam/Domain/Admin/Service/VisitLogService.java
package com.backend.Fiteam.Domain.Admin.Service;

import com.backend.Fiteam.Domain.Admin.Entity.VisitLog;
import com.backend.Fiteam.Domain.Admin.Repository.VisitLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VisitLogService {

    private final VisitLogRepository visitLogRepository;

    /**
     * 로그인 성공 등 호출 시, 오늘 방문 기록을 남깁니다.
     */
    public void logVisit(Integer userId) {
        VisitLog log = new VisitLog();
        log.setUserId(userId);
        log.setVisitDate(LocalDate.now());
        visitLogRepository.save(log);
    }
}
