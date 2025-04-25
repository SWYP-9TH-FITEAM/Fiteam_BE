package com.backend.Fiteam.User.Entity;

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

    @Column(name = "user_name", length = 30)
    private String userName;

    @Column(name = "profile_img_url", columnDefinition = "TEXT")
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

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    // Getters and Setters 생략 (Lombok 사용 시 @Getter, @Setter 추가)
}
