package com.backend.Fiteam.Domain.Team.Repository;

import com.backend.Fiteam.Domain.Team.Entity.TeamType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamTypeRepository extends JpaRepository<TeamType, Integer> {
}
