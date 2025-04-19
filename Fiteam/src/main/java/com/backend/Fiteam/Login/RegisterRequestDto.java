package com.backend.Fiteam.Login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
    private String name;
    private String phoneNumber;
}
