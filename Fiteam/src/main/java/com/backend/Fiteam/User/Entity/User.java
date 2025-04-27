package com.backend.Fiteam.User.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(length = 50)
    private String password;

    @Schema(description = "사용자 이름", example = "고양이")
    @Column(name = "user_name", length = 30)
    private String userName;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    @Column(name = "profile_img_url", length = 255)
    private String profileImgUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "kakao_id", length = 30)
    private String kakaoId;

    @Column(length = 50)
    private String job;

    @Column(length = 50)
    private String major;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(length = 200)
    private String url;

    @Column(name = "card_id1")
    private Integer cardId1;

    @Column(name = "card_id2")
    private Integer cardId2;

    @Schema(description = "캐릭터 카드 AI 분석결과", example = "ISTP 유형이면서 ~~한 특징이 있는 사람입니다.")
    @Column(length = 500)
    private String details;

    @Schema(description = "EI 성향 점수", example = "7")
    @Column(name = "num_EI")
    private Integer numEI;

    @Schema(description = "PD 성향 점수", example = "3")
    @Column(name = "num_PD")
    private Integer numPD;

    @Schema(description = "IA 성향 점수", example = "5")
    @Column(name = "num_IA")
    private Integer numIA;

    @Schema(description = "CL 성향 점수", example = "8")
    @Column(name = "num_CL")
    private Integer numCL;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    // Getters and Setters 생략 (Lombok 사용 시 @Getter, @Setter 추가)
}
