package com.backend.Fiteam.Domain.Admin.Controller;

import com.backend.Fiteam.Domain.Admin.Dto.AdminStatusResponseDto;
import com.backend.Fiteam.Domain.Admin.Dto.CreateManagerRequestDto;
import com.backend.Fiteam.Domain.Admin.Dto.CreateSystemNoticeRequestDto;
import com.backend.Fiteam.Domain.Admin.Dto.SystemNoticeResponseDto;
import com.backend.Fiteam.Domain.Admin.Service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Tag(name = "10. AdminController - Fiteam 운영자 계정")
@PreAuthorize("hasAuthority('Admin')")  // 모든 메서드는 Admin 권한 필요
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "1. 플랫폼 통계 조회", description = "가입한 사용자 수, 매니저 수, 오늘 방문자 수 등을 조회합니다.")
    @GetMapping("/visit/count")
    public ResponseEntity<AdminStatusResponseDto> getAdminStatistics() {
        AdminStatusResponseDto dto = adminService.getPlatformStatistics();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "2. 매니저 계정 생성", description = "Admin이 매니저 계정을 생성합니다.")
    @PostMapping("/new-manager")
    public ResponseEntity<String> createManager(@RequestBody CreateManagerRequestDto dto) {
        adminService.createManager(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Manager 계정 생성 성공");
    }

    @Operation(summary = "3-1. 시스템 공지사항 작성", description = "Admin이 새로운 시스템 공지사항을 작성합니다.")
    @PostMapping("/notices")
    public ResponseEntity<String> createSystemNotice(@AuthenticationPrincipal UserDetails userDetails,@RequestBody CreateSystemNoticeRequestDto dto) {
        Integer adminId = Integer.parseInt(userDetails.getUsername());
        adminService.createSystemNotice(dto, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body("시스템 공지사항 작성 성공");
    }

    @Operation(summary = "3-2. 시스템 공지사항 조회", description = "등록된 모든 시스템 공지사항 목록을 반환합니다.")
    @GetMapping("/system/notices")
    public ResponseEntity<List<SystemNoticeResponseDto>> getSystemNotices() {
        List<SystemNoticeResponseDto> list = adminService.getAllSystemNotices();
        return ResponseEntity.ok(list);
    }
}
