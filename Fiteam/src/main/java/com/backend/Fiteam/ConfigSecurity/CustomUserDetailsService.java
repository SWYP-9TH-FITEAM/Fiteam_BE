package com.backend.Fiteam.ConfigSecurity;

import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다: " + userId));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString()) // ID 기반
                .password(user.getPassword())
                .build();
    }

}
