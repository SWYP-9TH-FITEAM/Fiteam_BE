package com.backend.Fiteam.Login;

import jakarta.mail.MessagingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDto request) {
        authService.register(request);
        return ResponseEntity.ok("Register Success");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request) {
        String token = authService.login(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerification(@RequestBody EmailRequestDto dto) throws MessagingException {
        authService.sendAuthEmail(dto.getEmail());
        return ResponseEntity.ok("인증코드 발송 완료");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyDto dto) {
        boolean result = authService.verifyEmailCode(dto.getEmail(), dto.getCode());
        if (result) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 실패");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequestDto dto) {
        boolean verified = authService.verifyEmailCode(dto.getEmail(), dto.getCode());
        if (!verified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증코드가 일치하지 않습니다.");
        }

        authService.updatePassword(dto.getEmail(), dto.getNewPassword());
        return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다.");
    }

    // -- DTO
    @Getter
    @Setter
    public class EmailRequestDto {
        private String email;
    }

    @Getter
    @Setter
    public class EmailVerifyDto {
        private String email;
        private String code;
    }

    @Getter @Setter
    public class PasswordResetRequestDto {
        private String email;
        private String code;
        private String newPassword;
    }
}
