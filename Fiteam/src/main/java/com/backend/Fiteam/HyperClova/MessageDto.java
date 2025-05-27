package com.backend.Fiteam.HyperClova;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Chat 메시지 단일 객체")
public class MessageDto {
    @Schema(description = "role (system, user, assistant)", example = "user")
    private String role;
    @Schema(description = "메시지 내용", example = "안녕, 오늘 날씨 알려줘")
    private String content;
}
