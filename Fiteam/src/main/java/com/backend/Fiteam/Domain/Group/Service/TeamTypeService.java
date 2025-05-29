package com.backend.Fiteam.Domain.Group.Service;

import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamTypeService {
    private final TeamTypeRepository teamTypeRepository;

    /** startDatetime 을 현재 시각으로 덮어쓰기 */
    @Transactional
    public void markStartedNow(Integer teamTypeId) {
        TeamType tt = teamTypeRepository.findById(teamTypeId)
                .orElseThrow(() -> new NoSuchElementException("TeamType not found: " + teamTypeId));
        tt.setStartDatetime(LocalDateTime.now());
        teamTypeRepository.save(tt);
    }

    /** endDatetime 을 현재 시각으로 덮어쓰기 */
    @Transactional
    public void markEndedNow(Integer teamTypeId) {
        TeamType tt = teamTypeRepository.findById(teamTypeId)
                .orElseThrow(() -> new NoSuchElementException("TeamType not found: " + teamTypeId));
        tt.setEndDatetime(LocalDateTime.now());
        teamTypeRepository.save(tt);
    }
}

