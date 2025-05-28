package com.backend.Fiteam.ConfigSecurity;

import com.backend.Fiteam.Domain.Admin.Entity.Admin;
import com.backend.Fiteam.Domain.Admin.Repository.AdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Integer adminId = Integer.valueOf(username);
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 관리자입니다: " + adminId));

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(admin.getId()))
                .password(admin.getPassword())
                .roles("Admin")  // Spring Security의 ROLE_ prefix를 자동 붙임
                .build();
    }
}

