package com.backend.Fiteam.Domain.User.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class UserProfileDto {
    private String userName;
    private String profileImgUrl;
}
