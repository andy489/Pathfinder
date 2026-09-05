package com.pathfinder.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class UserEditDto {

    @NotBlank
    @Size(min = 4, max = 20)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    private String newPassword;

    @Size(min = 4, max = 20)
    private String confirmPassword;
}
