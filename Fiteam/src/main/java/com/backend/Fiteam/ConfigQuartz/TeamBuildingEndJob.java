package com.backend.Fiteam.ConfigQuartz;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Repository.ProjectGroupRepository;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TeamBuildingEndJob implements Job {

    @Autowired
    private GroupService groupService;
    @Autowired private ProjectGroupRepository projectGroupRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        Integer groupId = data.getInt("groupId");

        log.info("Quartz 종료 실행: groupId={}", groupId);

        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new JobExecutionException("Group not found: " + groupId));

        // 그룹 빌딩 종료
        groupService.closeTeamBuilding(group);
    }
}

