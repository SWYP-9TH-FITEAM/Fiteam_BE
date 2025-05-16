package com.backend.Fiteam.Domain.Group.Repository;

import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
    boolean existsByGroupIdAndUserId(Integer groupId, Integer id);
    Optional<GroupMember> findByUserIdAndGroupId(Integer senderId, Integer groupId);

    int countByGroupId(Integer groupId);

    Optional<GroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);

    List<GroupMember> findByGroupId(Integer groupId);

    boolean existsByGroupIdAndUserIdAndIsAcceptedTrue(Integer groupId, Integer userId);

    long countByTeamId(Integer primaryTeamId);

    List<GroupMember> findAllByTeamId(Integer secondaryTeamId);


    List<GroupMember> findAllByGroupId(Integer groupId);

    List<GroupMember> findAllByUserIdAndIsAcceptedTrue(Integer userId);

    List<GroupMember> findAllByUserIdAndIsAcceptedFalse(Integer userId);

    List<GroupMember> findAllByGroupIdAndIsAcceptedTrue(Integer groupId);

    Optional<GroupMember> findByUserIdAndTeamId(Integer userId, Integer teamId);


    int countByGroupIdAndIsAcceptedTrue(Integer id);

    Optional<GroupMember> findByGroupIdAndUserIdAndIsAcceptedTrue(Integer groupId, Integer userId);

    Optional<GroupMember> findByUserIdAndGroupIdAndIsAcceptedTrueAndTeamIdNotNull(Integer userId, Integer groupId);

    List<GroupMember> findAllByGroupIdAndTeamId(Integer groupId, Integer teamId);

    Optional<GroupMember> findByUserIdAndGroupIdAndIsAcceptedTrue(Integer userId, Integer groupId);

    Optional<GroupMember> findByIdAndIsAcceptedTrue(Integer memberId);
}
