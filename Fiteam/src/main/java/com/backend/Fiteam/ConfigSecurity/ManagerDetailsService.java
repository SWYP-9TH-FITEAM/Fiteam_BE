package com.backend.Fiteam.ConfigSecurity;

import com.backend.Fiteam.Domain.Group.Entity.Manager;
import com.backend.Fiteam.Domain.Group.Repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerDetailsService implements UserDetailsService {
    private final ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String id) {
        Manager m = managerRepository.findById(Integer.parseInt(id))
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 매니저: "+id));

        return org.springframework.security.core.userdetails.User.builder()
                .username(m.getId().toString())
                .password(m.getPassword())
                .roles("MANAGER")       // ← ROLE_MANAGER 권한 명시
                .build();
    }
}

