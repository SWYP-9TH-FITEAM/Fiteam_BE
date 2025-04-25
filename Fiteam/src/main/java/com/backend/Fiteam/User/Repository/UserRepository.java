package com.backend.Fiteam.User.Repository;

import com.backend.Fiteam.User.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
}
