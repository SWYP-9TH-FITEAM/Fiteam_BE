package com.backend.Fiteam.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email); // 이메일로 사용자 조회

    boolean existsByEmail(String email);
}
