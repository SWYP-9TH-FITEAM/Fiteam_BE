package com.backend.Fiteam.ConfigSecurity;

import com.backend.Fiteam.Domain.Group.Entity.Manager;
import com.backend.Fiteam.Domain.Group.Repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;

@Service
@RequiredArgsConstructor
public class ManagerDetailsService implements UserDetailsService {

    private final ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        // id 기반으로 조회
        Manager manager = managerRepository.findById(Integer.parseInt(id))
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 매니저입니다: " + id));

        return User.builder()
                .username(id)  // JWT에서 꺼낸 subject = id
                .password(manager.getPassword())
                .build();
    }

}
