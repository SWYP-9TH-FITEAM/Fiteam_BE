package com.backend.Fiteam.Domain.Group.Repository;

import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
    boolean existsByGroupIdAndUserId(Integer groupId, Integer id);

    int countByGroupId(Integer groupId);

    Optional<GroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);

    List<GroupMember> findByGroupId(Integer groupId);

    boolean existsByGroupIdAndUserIdAndIsAcceptedTrue(Integer groupId, Integer userId);

    Optional<GroupMember> findTopByUserIdAndIsAcceptedTrue(Integer targetUserId);

    List<GroupMember> findAllByUserIdAndIsAcceptedTrue(Integer userId);
    List<GroupMember> findAllByUserIdAndIsAcceptedFalse(Integer userId);
}
