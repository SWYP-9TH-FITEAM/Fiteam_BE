package com.backend.Fiteam.Domain.Team.Repository;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.backend.Fiteam.Domain.Team.Entity.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Integer> {

    List<Team> findAllByGroupId(Integer groupId);

    List<Team> findByGroupId(Integer groupId);

    List<Team> findAllByGroupIdAndTeamStatus(Integer groupId, TeamStatus status);
}
