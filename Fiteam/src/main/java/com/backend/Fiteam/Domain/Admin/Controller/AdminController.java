package com.backend.Fiteam.Domain.Admin.Controller;

import com.backend.Fiteam.Domain.Admin.Dto.AdminStatusResponseDto;
import com.backend.Fiteam.Domain.Admin.Dto.CreateManagerRequestDto;
import com.backend.Fiteam.Domain.Admin.Dto.CreateSystemNoticeRequestDto;
import com.backend.Fiteam.Domain.Admin.Dto.SystemNoticeResponseDto;
import com.backend.Fiteam.Domain.Admin.Service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Tag(name = "10. AdminController - Fiteam 운영자 계정")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "1. 플랫폼 통계 조회", description = "가입한 사용자 수, 매니저 수, 오늘 방문자 수 등을 조회합니다.")
    @GetMapping("/visit/count")
    public ResponseEntity<AdminStatusResponseDto> getAdminStatistics() {
        try {
            AdminStatusResponseDto dto = adminService.getPlatformStatistics();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/new-manager")
    public ResponseEntity<String> createManager(@RequestBody CreateManagerRequestDto dto) {
        try {
            adminService.createManager(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Manager 계정 생성 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류");
        }
    }

    @Operation(summary = "3-1. 시스템 공지사항 작성", description = "Admin이 새로운 시스템 공지사항을 작성합니다.")
    @PostMapping("/notices")
    public ResponseEntity<String> createSystemNotice(@RequestBody CreateSystemNoticeRequestDto dto) {
        try {
            adminService.createSystemNotice(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("시스템 공지사항 작성 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류");
        }
    }

    @Operation(summary = "3-2. 시스템 공지사항 조회", description = "등록된 모든 시스템 공지사항 목록을 반환합니다.")
    @GetMapping("/notices")
    public ResponseEntity<List<SystemNoticeResponseDto>> getSystemNotices() {
        try {
            List<SystemNoticeResponseDto> list = adminService.getAllSystemNotices();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
