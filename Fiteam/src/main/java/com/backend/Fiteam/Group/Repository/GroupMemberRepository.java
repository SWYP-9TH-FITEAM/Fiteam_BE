package com.backend.Fiteam.Group.Repository;

import com.backend.Fiteam.Group.Entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
}
