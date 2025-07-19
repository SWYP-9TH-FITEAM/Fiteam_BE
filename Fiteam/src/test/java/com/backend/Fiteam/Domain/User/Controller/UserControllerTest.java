package com.backend.Fiteam.Domain.User.Controller;

import com.backend.Fiteam.Domain.User.Dto.UserSettingsRequestDto;
import com.backend.Fiteam.Domain.User.Service.UserService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /v1/user/savecard - 테스트 결과 저장")
    @WithMockUser(username = "1", roles = {"USER"})
    void saveTestResult() throws Exception {
        // given: 샘플 응답 데이터
        List<Map<String, Integer>> answers = List.of(Map.of("questionId", 1, "answer", 2));
        String json = objectMapper.writeValueAsString(answers);

        // when & then: API 호출 및 응답 검증
        mockMvc.perform(post("/v1/user/savecard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("테스트 결과가 성공적으로 저장되었습니다."));

        // verify: 서비스 메서드가 호출되었는지 확인
        verify(userService, times(1)).saveCharacterTestResult(1, answers);
    }

    @Test
    @DisplayName("PATCH /v1/user/accept/{groupId} - 그룹 참여 수락")
    @WithMockUser(username = "1", roles = {"USER"})
    void acceptInvitation() throws Exception {
        int groupId = 5;

        // when & then: 그룹 초대 수락 API 호출 및 응답 검증
        mockMvc.perform(patch("/v1/user/accept/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(content().string("그룹에 참여했습니다."));

        // verify: 서비스의 수락 메서드 호출 확인
        verify(userService, times(1)).acceptGroupInvitation(eq(groupId), eq(1));
    }

    @Test
    @DisplayName("PATCH /v1/user/settings - 사용자 설정 업데이트")
    @WithMockUser(username = "1", roles = {"USER"})
    void updateUserSettings() throws Exception {
        // given: 업데이트할 유저 설정 정보 JSON
        String settingsJson = """
            {
              \"phoneNumber\": \"010-0000-0000\",
              \"kakaoId\": \"test_kakao\",
              \"job\": \"Developer\",
              \"major\": \"CS\",
              \"introduction\": \"Hello, this is a test intro.\",
              \"url\": \"https://example.com\"
            }
            """;

        // when & then: 설정 업데이트 API 호출 및 상태 검증
        mockMvc.perform(patch("/v1/user/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(settingsJson))
                .andExpect(status().isOk());

        // verify: 서비스 업데이트 메서드가 호출되었는지 확인
        ArgumentCaptor<UserSettingsRequestDto> captor = ArgumentCaptor.forClass(UserSettingsRequestDto.class);
        verify(userService, times(1)).updateUserSettings(eq(1), captor.capture());
        UserSettingsRequestDto captured = captor.getValue();
        // 필드 값 검증
        assert captured.getPhoneNumber().equals("010-0000-0000");
        assert captured.getKakaoId().equals("test_kakao");
        assert captured.getJob().equals("Developer");
        assert captured.getMajor().equals("CS");
        assert captured.getIntroduction().startsWith("Hello");
        assert captured.getUrl().contains("example.com");
    }
}
