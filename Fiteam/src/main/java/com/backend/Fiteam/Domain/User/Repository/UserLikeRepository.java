package com.backend.Fiteam.Domain.User.Repository;

import com.backend.Fiteam.Domain.User.Entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLikeRepository extends JpaRepository<UserLike, Integer> {
}
