package com.backend.Fiteam.Domain.Group.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Manager")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(name = "manager_name", length = 30)
    private String managerName;

    @Column(length = 50)
    private String organization;

    @Schema(description = "매니저 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    @Column(name = "profile_img_url", length = 255)
    private String profileImgUrl;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;
}
