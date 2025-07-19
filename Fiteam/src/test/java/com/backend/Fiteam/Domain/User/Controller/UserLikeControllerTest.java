package com.backend.Fiteam.Domain.User.Controller;

import com.backend.Fiteam.Domain.User.Dto.UserLikeRequestDto;
import com.backend.Fiteam.Domain.User.Service.UserLikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserLikeService userLikeService;

    @Test
    @DisplayName("POST /v1/like/add - 좋아요 등록")
    @WithMockUser(username = "1", roles = {"USER"})
    void likeUser() throws Exception {
        int targetUserId = 5;
        // given: 좋아요 요청 DTO
        UserLikeRequestDto requestDto = new UserLikeRequestDto();
        requestDto.setReceiverId(targetUserId);
        requestDto.setGroupId(2);
        requestDto.setNumber(1);
        requestDto.setMemo("이분이 FE중 1픽!");
        String json = objectMapper.writeValueAsString(requestDto);

        // when & then: API 호출 및 응답 검증
        mockMvc.perform(post("/v1/like/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("좋아요가 등록되었습니다."));

        // verify: 서비스 호출 및 DTO 전달값 검증
        ArgumentCaptor<UserLikeRequestDto> captor = ArgumentCaptor.forClass(UserLikeRequestDto.class);
        verify(userLikeService, times(1)).likeUser(eq(1), captor.capture());
        UserLikeRequestDto captured = captor.getValue();
        assert captured.getReceiverId().equals(targetUserId);
        assert captured.getGroupId().equals(2);
        assert captured.getNumber().equals(1);
        assert captured.getMemo().equals("이분이 FE중 1픽!");
    }

    @Test
    @DisplayName("DELETE /v1/like/unlike/{likeId} - 좋아요 취소")
    @WithMockUser(username = "1", roles = {"USER"})
    void unlikeUser() throws Exception {
        int likeId = 3;

        // when & then: 좋아요 취소 API 호출 및 응답 검증
        mockMvc.perform(delete("/v1/like/unlike/{likeId}", likeId))
                .andExpect(status().isOk())
                .andExpect(content().string("좋아요가 취소되었습니다."));

        // verify: 서비스 호출 확인
        verify(userLikeService, times(1)).unlikeUser(eq(1), eq(likeId));
    }
}
