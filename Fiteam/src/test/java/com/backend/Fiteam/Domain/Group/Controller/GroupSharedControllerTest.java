package com.backend.Fiteam.Domain.Group.Controller;

import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.backend.Fiteam.Domain.Admin.Dto.SystemNoticeResponseDto;
import com.backend.Fiteam.Domain.Admin.Service.AdminService;
import com.backend.Fiteam.Domain.Group.Dto.*;
import com.backend.Fiteam.Domain.Group.Service.GroupNoticeService;
import com.backend.Fiteam.Domain.Group.Service.GroupService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupSharedController.class)
@WithMockUser(username = "1")  // principal.getUsername() → "1"
class GroupSharedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private GroupNoticeService groupNoticeService;

    @MockitoBean
    private AdminService adminService;

    @Test
    void getGroupDetail_shouldReturnDetail() throws Exception {
        int groupId = 3;
        GroupDetailResponseDto dto = new GroupDetailResponseDto(
                groupId,
                "스위프9기",
                "함께 성장",
                80,
                "오픈카톡",
                Timestamp.valueOf("2025-04-26 10:30:00"),
                2,
                "랜덤 자동 배정",
                "규칙에 따라 랜덤",
                LocalDateTime.of(2025,5,15,15,0,0),
                LocalDateTime.of(2025,5,20,18,0,0),
                3,
                6,
                false,
                "{\"FE\":2,\"BE\":2}"
        );
        Mockito.doNothing().when(groupService).authorizeManagerOrMember(anyInt(), eq(groupId));
        Mockito.when(groupService.getGroupDetail(groupId)).thenReturn(dto);

        mockMvc.perform(get("/v1/group/{groupId}/data", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId))
                .andExpect(jsonPath("$.name").value("스위프9기"))
                .andExpect(jsonPath("$.teamTypeName").value("랜덤 자동 배정"));
        // GroupDetailResponseDto 필드 참고 :contentReference[oaicite:8]{index=8}, 엔드포인트 경로 :contentReference[oaicite:9]{index=9}
    }

    @Test
    void getGroupPositions_shouldReturnList() throws Exception {
        int groupId = 5;
        List<String> positions = List.of("PM","FE");
        Mockito.doNothing().when(groupService).validateGroupMembership(anyInt(), eq(groupId));
        Mockito.when(groupService.getPositionListForGroup(groupId)).thenReturn(positions);

        mockMvc.perform(get("/v1/group/{groupId}/positions", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("PM"))
                .andExpect(jsonPath("$[1]").value("FE"));
        // 위치 리스트 반환 :contentReference[oaicite:10]{index=10}
    }

    @Test
    void getGroupPositions_whenJsonError_thenBadRequest() throws Exception {
        int groupId = 5;
        Mockito.doNothing().when(groupService).validateGroupMembership(anyInt(), eq(groupId));
        Mockito.when(groupService.getPositionListForGroup(groupId))
                .thenThrow(new JsonProcessingException("invalid JSON") {});

        mockMvc.perform(get("/v1/group/{groupId}/positions", groupId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGroupNotices_shouldReturnList() throws Exception {
        int groupId = 7;
        GroupNoticeSummaryDto summary = GroupNoticeSummaryDto.builder()
                .id(10)
                .groupId(groupId)
                .groupName("테스트그룹")
                .title("공지제목")
                .createdAt(Timestamp.valueOf("2025-07-12 09:00:00"))
                .build();
        Mockito.when(groupNoticeService.getNoticesForMember(anyInt(), eq(groupId)))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/v1/group/{groupId}/notice/list", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("공지제목"));
    }

    @Test
    void getNoticeById_shouldReturnDetail() throws Exception {
        int noticeId = 20;
        GroupNoticeDetailDto detail = GroupNoticeDetailDto.builder()
                .id(noticeId)
                .managerId(2)
                .groupId(7)
                .groupName("테스트그룹")
                .title("상세공지")
                .context("내용")
                .createdAt(Timestamp.valueOf("2025-07-12 08:00:00"))
                .build();
        Mockito.when(groupNoticeService.getNoticeById(noticeId)).thenReturn(detail);

        mockMvc.perform(get("/v1/group/notice/{noticeId}/detail", noticeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.context").value("내용"))
                .andExpect(jsonPath("$.groupName").value("테스트그룹"));
    }

    @Test
    void getSystemNotices_shouldReturnList() throws Exception {
        SystemNoticeResponseDto sys = SystemNoticeResponseDto.builder()
                .id(3)
                .title("시스템")
                .content("점검")
                .createdAt(LocalDateTime.of(2025, 7, 12, 7, 0, 0))
                .build();
        Mockito.when(adminService.getAllSystemNotices()).thenReturn(List.of(sys));

        mockMvc.perform(get("/v1/system/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("시스템"));
    }

    @Test
    void getGroupMembers_shouldReturnList() throws Exception {
        int groupId = 9;
        GroupMemberResponseDto member = GroupMemberResponseDto.builder()
                .userId(11)
                .memberId(22)
                .userName("홍길동")
                .profileImageUrl("url")
                .cardId1(3)
                .position("PM")
                .teamStatus(TeamStatus.RECRUITING)
                .teamId(5)
                .likeId(null)
                .build();
        Mockito.doNothing().when(groupService).authorizeManagerOrMember(anyInt(), eq(groupId));
        Mockito.when(groupService.getGroupMembers(anyInt(), eq(groupId), eq(true)))
                .thenReturn(List.of(member));

        mockMvc.perform(get("/v1/group/{groupId}/members", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("홍길동"))
                .andExpect(jsonPath("$[0].position").value("PM"));
        // GroupMemberResponseDto 구조 참고 :contentReference[oaicite:11]{index=11}
    }
}
