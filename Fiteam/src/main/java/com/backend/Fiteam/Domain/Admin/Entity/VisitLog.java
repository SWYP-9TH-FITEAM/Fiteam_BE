package com.backend.Fiteam.Domain.Admin.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "visit_log")
@Getter
@Setter
@NoArgsConstructor
public class VisitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 방문한 사용자 ID */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** 방문 일자 (yyyy-MM-dd) */
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;
}

