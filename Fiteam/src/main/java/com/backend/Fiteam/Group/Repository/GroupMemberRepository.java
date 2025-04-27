package com.backend.Fiteam.Group.Repository;

import com.backend.Fiteam.Group.Entity.GroupMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
    boolean existsByGroupIdAndUserId(Integer groupId, Integer id);

    int countByGroupId(Integer groupId);

    Optional<GroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);
}
