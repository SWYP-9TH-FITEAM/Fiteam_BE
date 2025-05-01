package com.backend.Fiteam.ConfigSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// PW 생성 테스트 파일
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 테스트용 평문 비밀번호
        String plainPassword = "pw1";

        // 암호화된 비밀번호 생성
        String hashedPassword = encoder.encode(plainPassword);

        System.out.println("테스트용 비밀번호 (평문): " + plainPassword);
        System.out.println("테스트용 비밀번호 (해시): " + hashedPassword);
    }
}