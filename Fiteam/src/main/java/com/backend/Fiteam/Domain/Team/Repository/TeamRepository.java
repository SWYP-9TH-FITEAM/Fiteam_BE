package com.backend.Fiteam.Domain.Team.Repository;

import com.backend.Fiteam.Domain.Team.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Integer> {
}
