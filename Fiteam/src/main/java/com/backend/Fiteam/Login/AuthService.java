package com.backend.Fiteam.Login;

import com.backend.Fiteam.User.Entity.User;
import com.backend.Fiteam.User.Repository.UserRepository;
import com.backend.Fiteam.ConfigSecurity.JwtTokenProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    // 1. 로그인 회원가입 로직
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userName(request.getUsername())
                .build();

        userRepository.save(user);
    }

    public String login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("email error"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("password error");
        }

        return jwtTokenProvider.createToken(user.getId());
    }


    // 2. Email 인증하기 로직
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    private final Map<String, String> emailAuthMap = new HashMap<>(); // 이메일-코드 매핑

    // 인증코드 생성- 6자리 숫자 방식
    public String createAuthCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // 이메일 전송
    public void sendAuthEmail(String toEmail) throws MessagingException {
        String code = createAuthCode();
        emailAuthMap.put(toEmail, code);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("[프로젝트] 이메일 인증코드");
        try {
            helper.setFrom(fromEmail, "프로젝트 인증팀"); // ← 이름 추가
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        helper.setText("<h3>인증코드: <b>" + code + "</b></h3>", true);

        mailSender.send(message);
    }

    // 인증코드 검증
    public boolean verifyEmailCode(String email, String code) {
        return code.equals(emailAuthMap.get(email));
    }

    // 비밀번호 재설정
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
