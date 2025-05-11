package com.backend.Fiteam.ConfigQuartz;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import com.backend.Fiteam.Domain.Group.Entity.TeamType;
import com.backend.Fiteam.Domain.Team.Repository.TeamTypeRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
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

    /**
     * groupId, teamTypeId 기준으로 Job 등록
     */
    public void scheduleTeamBuilding(ProjectGroup group) throws SchedulerException {
        TeamType type = teamTypeRepository.findById(group.getTeamMakeType())
                .orElseThrow(() -> new IllegalArgumentException("TeamType not found"));

        LocalDateTime fireTime = type.getStartDatetime();
        if (type.getBuildingDone() || fireTime.isBefore(LocalDateTime.now())) {
            return;  // 이미 실행됐거나 과거라면 등록 안 함
        }

        JobDetail job = JobBuilder.newJob(TeamBuildingJob.class)
                .withIdentity("job_group_"+group.getId(), "teamBuilding")
                .usingJobData("groupId", group.getId())
                .usingJobData("teamTypeId", type.getId())
                .build();

        Trigger trig = TriggerBuilder.newTrigger()
                .withIdentity("trigger_group_"+group.getId(), "teamBuilding")
                .startAt(Timestamp.valueOf(fireTime))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        // 기존에 등록된 게 있으면 제거
        if (scheduler.checkExists(job.getKey())) {
            scheduler.deleteJob(job.getKey());
        }
        scheduler.scheduleJob(job, trig);
    }
}
