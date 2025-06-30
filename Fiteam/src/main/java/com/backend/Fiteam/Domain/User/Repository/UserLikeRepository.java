package com.backend.Fiteam.Domain.User.Repository;

import com.backend.Fiteam.Domain.User.Entity.UserLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLikeRepository extends JpaRepository<UserLike, Integer> {
    Optional<UserLike> findBySenderIdAndReceiverIdAndGroupId(Integer senderId, Integer receiverId, Integer groupId);
    List<UserLike> findAllBySenderId(Integer senderId);

    void deleteAllByGroupId(Integer groupId);

    boolean existsBySenderIdAndReceiverIdAndGroupId(Integer senderId, Integer receiverId, Integer groupId);

    List<UserLike> findBySenderIdAndGroupId(Integer userId, Integer groupId);
}
