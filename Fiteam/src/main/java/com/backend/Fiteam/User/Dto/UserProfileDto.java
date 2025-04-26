package com.backend.Fiteam.User.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class UserProfileDto {
    private String userName;
    private String profileImgUrl;
}
