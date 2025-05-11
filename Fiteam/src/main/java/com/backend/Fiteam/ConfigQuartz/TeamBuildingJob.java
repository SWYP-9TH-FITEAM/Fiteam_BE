package com.backend.Fiteam.ConfigQuartz;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@NoArgsConstructor      // ← 기본 생성자 추가
public class TeamBuildingJob implements Job {

    @Autowired private GroupService groupService;
    @Autowired private ProjectGroupRepository projectGroupRepository;
    @Autowired private TeamTypeRepository teamTypeRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        Integer groupId    = data.getInt("groupId");
        Integer teamTypeId = data.getInt("teamTypeId");

        log.info("Quartz 실행: groupId={}, teamTypeId={}", groupId, teamTypeId);

        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new JobExecutionException("Group not found: " + groupId));
        groupService.RandomTeamBuilding(group);

        teamTypeRepository.findById(teamTypeId).ifPresent(tt -> {
            tt.setBuildingDone(true);
            teamTypeRepository.save(tt);
        });
    }
}
