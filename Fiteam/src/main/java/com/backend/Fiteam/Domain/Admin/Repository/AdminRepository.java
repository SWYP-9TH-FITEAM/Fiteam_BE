// src/main/java/com/backend/Fiteam/Domain/Admin/Repository/AdminRepository.java
package com.backend.Fiteam.Domain.Admin.Repository;

import com.backend.Fiteam.Domain.Admin.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
}
