package com.backend.Fiteam.Domain.Group.Repository;

import com.backend.Fiteam.Domain.Group.Entity.ProjectGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, Integer> {
    boolean existsByManagerIdAndName(Integer managerId, String name);
    List<ProjectGroup> findAllByManagerId(Integer managerId);

}
