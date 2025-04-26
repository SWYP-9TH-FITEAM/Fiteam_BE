package com.backend.Fiteam.User.Dto;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserNotifyDto {
    private String senderType;
    private Integer senderId;
    private final String content;
    private final Boolean isRead;
    private final Timestamp createdAt;
}
