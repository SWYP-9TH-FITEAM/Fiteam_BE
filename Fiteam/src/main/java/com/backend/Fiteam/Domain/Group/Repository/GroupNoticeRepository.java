package com.backend.Fiteam.Domain.Group.Repository;

import com.backend.Fiteam.Domain.Group.Entity.GroupNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupNoticeRepository extends JpaRepository<GroupNotice, Integer> {
    List<GroupNotice> findAllByManagerIdOrderByCreatedAtDesc(Integer managerId);
}
