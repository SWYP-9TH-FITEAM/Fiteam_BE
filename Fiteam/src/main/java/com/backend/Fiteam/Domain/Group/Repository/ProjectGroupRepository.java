package com.backend.Fiteam.Domain.Group.Repository;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, Integer> {
    boolean existsByManagerIdAndName(Integer managerId, String name);
    List<ProjectGroup> findAllByManagerId(Integer managerId);

    @Query("SELECT pg FROM ProjectGroup pg JOIN FETCH pg.teamMakeType tt WHERE pg.managerId = :managerId ")
    List<ProjectGroup> findAllWithTeamTypeByManagerId(@Param("managerId") Integer managerId);

}
