package com.backend.Fiteam.Domain.Admin.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateManagerRequestDto {

    @Schema(description = "매니저 이메일", example = "mgr@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "P@ssw0rd!")
    private String password;

    @Schema(description = "매니저 이름", example = "홍매니저")
    private String managerName;

    @Schema(description = "소속 기관", example = "Fiteam Inc.")
    private String organization;
}
