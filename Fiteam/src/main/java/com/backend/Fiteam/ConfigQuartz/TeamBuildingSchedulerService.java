package com.backend.Fiteam.ConfigQuartz;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamBuildingSchedulerService {
    private final Scheduler scheduler;
    private final TeamTypeRepository teamTypeRepository;

    public void scheduleTeamBuilding(ProjectGroup group) throws SchedulerException {
        TeamType type = teamTypeRepository.findById(group.getTeamMakeType())
                .orElseThrow(() -> new IllegalArgumentException("TeamType not found"));

        LocalDateTime startTime = type.getStartDatetime();
        LocalDateTime endTime   = type.getEndDatetime();

        // 1) 시작 트리거
        if (!type.getBuildingDone() && startTime.isAfter(LocalDateTime.now())) {
            scheduleJob(
                    "job_group_" + group.getId() + "_start",
                    "trigger_group_" + group.getId() + "_start",
                    TeamBuildingJob.class,
                    group.getId(), type.getId(),
                    Timestamp.valueOf(startTime)
            );
        }

        // 2) 종료 트리거
        if (endTime.isAfter(LocalDateTime.now())) {
            scheduleJob(
                    "job_group_" + group.getId() + "_end",
                    "trigger_group_" + group.getId() + "_end",
                    TeamBuildingEndJob.class,
                    group.getId(), type.getId(),
                    Timestamp.valueOf(endTime)
            );
        }
    }

    private void scheduleJob(String jobName, String trigName,
            Class<? extends Job> jobClass,
            Integer groupId, Integer teamTypeId,
            Timestamp fireAt) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, "teamBuilding")
                .usingJobData("groupId", groupId)
                .usingJobData("teamTypeId", teamTypeId)
                .build();

        Trigger trig = TriggerBuilder.newTrigger()
                .withIdentity(trigName, "teamBuilding")
                .startAt(fireAt)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        if (scheduler.checkExists(job.getKey())) {
            scheduler.deleteJob(job.getKey());
        }
        scheduler.scheduleJob(job, trig);
    }
}

