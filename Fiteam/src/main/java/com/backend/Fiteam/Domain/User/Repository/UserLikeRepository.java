package com.backend.Fiteam.Domain.User.Repository;

import com.backend.Fiteam.Domain.User.Entity.UserLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLikeRepository extends JpaRepository<UserLike, Integer> {


    Optional<UserLike> findBySenderIdAndReceiverId(Integer senderId, Integer receiverId);
    boolean existsBySenderIdAndReceiverId(Integer senderId, Integer receiverId);

}
