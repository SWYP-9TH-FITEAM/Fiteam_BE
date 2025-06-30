package com.backend.Fiteam.ConfigQuartz;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Service.TeamBuildingService;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@NoArgsConstructor
public class TeamBuildingJob implements Job {

    @Autowired private GroupService groupService;
    @Autowired private ProjectGroupRepository projectGroupRepository;
    @Autowired private TeamTypeRepository teamTypeRepository;
    @Autowired private TeamBuildingService teamBuildingService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        Integer groupId    = data.getInt("groupId");
        Integer teamTypeId = data.getInt("teamTypeId");

        log.info("Quartz 실행: groupId={}, teamTypeId={}", groupId, teamTypeId);

        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new JobExecutionException("Group not found: " + groupId));


        TeamType tt = teamTypeRepository.findById(teamTypeId)
                .orElseThrow(() -> new JobExecutionException("TeamType not found: " + teamTypeId));

        if (Boolean.TRUE.equals(tt.getPositionBased())) {
            // position 기반 모드: '대기중' → '모집중' 으로 전환
            teamBuildingService.openPositionBasedRequests(group);
        } else {
            // 랜덤 자동 팀빌딩
            teamBuildingService.RandomTeamBuilding(group);
        }

        // 한 번만 실행되도록 플래그 설정
        tt.setBuildingDone(true);
        teamTypeRepository.save(tt);
    }
}
