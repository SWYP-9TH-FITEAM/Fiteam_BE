package com.backend.Fiteam.Team.Repository;

import com.backend.Fiteam.Team.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Integer> {
}
