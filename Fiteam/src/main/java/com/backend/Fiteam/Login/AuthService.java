package com.backend.Fiteam.Login;

import com.backend.Fiteam.Domain.Admin.Entity.Admin;
import com.backend.Fiteam.Domain.Admin.Repository.AdminRepository;
import com.backend.Fiteam.Domain.Admin.Service.VisitLogService;
import com.backend.Fiteam.Domain.Group.Entity.Manager;
import com.backend.Fiteam.Domain.Group.Repository.ManagerRepository;
import com.backend.Fiteam.Domain.User.Entity.User;
import com.backend.Fiteam.Domain.User.Repository.UserRepository;
import com.backend.Fiteam.ConfigSecurity.JwtTokenProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    // 1. 로그인 회원가입 로직
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ManagerRepository managerRepository;
    private final VisitLogService visitLogService;
    private final AdminRepository adminRepository;

    public void register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userName(request.getUsername())
                .phoneNumber(request.getPhoneNumber())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        userRepository.save(user);
    }


    public LoginResponseDto login(String email, String password) {
        // 1) Admin 먼저 조회
        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            if (!passwordEncoder.matches(password, admin.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            String token = jwtTokenProvider.createToken(admin.getId(), "admin");
            return new LoginResponseDto(token, "admin");
        }


        // 2. Manager 먼저 조회
        Manager manager = managerRepository.findByEmail(email).orElse(null);
        if (manager != null) {
            if (!passwordEncoder.matches(password, manager.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            String token = jwtTokenProvider.createToken(manager.getId(), "manager");
            return new LoginResponseDto(token, "manager");
        }

        // 3. User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        visitLogService.logVisit(user.getId());
        String token = jwtTokenProvider.createToken(user.getId(), "user");
        return new LoginResponseDto(token, "user");
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
