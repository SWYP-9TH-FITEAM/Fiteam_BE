package com.backend.Fiteam.Domain.Team.Repository;

import com.backend.Fiteam.Domain.Team.Entity.TeamRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRequestRepository extends JpaRepository<TeamRequest, Integer> {

    boolean existsBySenderIdAndReceiverIdAndGroupId(Integer senderId, Integer receiverId, Integer groupId);

    List<TeamRequest> findAllByReceiverId(Integer receiverId);

    Optional<TeamRequest> findBySenderIdAndReceiverId(Integer senderId, Integer receiverId);

    List<TeamRequest> findByTeamId(Integer secondaryTeamId);

    void deleteAllByTeamId(Integer id);
}

