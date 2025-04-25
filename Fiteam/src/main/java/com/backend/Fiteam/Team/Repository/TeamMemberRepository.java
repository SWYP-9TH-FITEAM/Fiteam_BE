package com.backend.Fiteam.Team.Repository;

import com.backend.Fiteam.Team.Entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {
}
