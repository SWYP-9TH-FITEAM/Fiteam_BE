package com.backend.Fiteam.Domain.User.Repository;

import com.backend.Fiteam.Domain.Group.Entity.GroupMember;
import com.backend.Fiteam.Domain.User.Entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(Integer Id);
    Optional<User> findByEmail(String email);
}
