// src/main/java/com/backend/Fiteam/Domain/Admin/Service/AdminService.java
package com.backend.Fiteam.Domain.Admin.Service;

import com.backend.Fiteam.Domain.Admin.Dto.AdminStatusResponseDto;
import com.backend.Fiteam.Domain.Admin.Dto.CreateManagerRequestDto;
import com.backend.Fiteam.Domain.Admin.Dto.CreateSystemNoticeRequestDto;
import com.backend.Fiteam.Domain.Admin.Dto.SystemNoticeResponseDto;
import com.backend.Fiteam.Domain.Admin.Entity.Admin;
import com.backend.Fiteam.Domain.Admin.Entity.SystemNotice;
import com.backend.Fiteam.Domain.Admin.Repository.AdminRepository;
import com.backend.Fiteam.Domain.Admin.Repository.SystemNoticeRepository;
import com.backend.Fiteam.Domain.Admin.Repository.VisitLogRepository;
import com.backend.Fiteam.Domain.Group.Entity.Manager;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.backend.Fiteam.Domain.Group.Repository.ManagerRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final VisitLogRepository visitLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemNoticeRepository systemNoticeRepository;
    private final AdminRepository adminRepository;

    /**
     * 플랫폼 전체 통계: 총 사용자 수, 총 매니저 수, 오늘 방문자 수
     */
    public AdminStatusResponseDto getPlatformStatistics() {
        long totalUsers = userRepository.count();
        long totalManagers = managerRepository.count();
        long todayVisitors = visitLogRepository.countDistinctUserIdByVisitDate(LocalDate.now());

        return AdminStatusResponseDto.builder()
                .totalUsers(totalUsers)
                .totalManagers(totalManagers)
                .todayVisitors(todayVisitors)
                .build();
    }


    public void createManager(CreateManagerRequestDto dto) {
        // 이메일 중복 체크
        if (managerRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 매니저 이메일입니다.");
        }

        // 새 Manager 엔티티 생성 및 저장
        Manager manager = Manager.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .managerName(dto.getManagerName())
                .organization(dto.getOrganization())
                .build();

        managerRepository.save(manager);
    }

    public void createSystemNotice(CreateSystemNoticeRequestDto dto) {
        SystemNotice notice = SystemNotice.builder()
                .adminId(dto.getAdminId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        systemNoticeRepository.save(notice);
    }

    public List<SystemNoticeResponseDto> getAllSystemNotices() {
        return systemNoticeRepository.findAll().stream()
                .map(n -> {
                    // Admin 이름 조회
                    Admin admin = adminRepository.findById(n.getAdminId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Admin ID: " + n.getAdminId()));
                    return SystemNoticeResponseDto.builder()
                            .id(n.getId())
                            .adminName(admin.getName())   // ← 여기서 이름 사용
                            .title(n.getTitle())
                            .content(n.getContent())
                            .createdAt(n.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
