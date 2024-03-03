package com.pathfinder.model.dto;

import com.pathfinder.model.validation.register.FieldMatch;
import com.pathfinder.model.validation.register.MinAge;
import com.pathfinder.model.validation.register.UniqueEmail;
import com.pathfinder.model.validation.register.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldMatch(
        firstField = "password",
        secondField = "confirmPassword",
        message = "{user.password.mismatch}"
)
public class UserRegistrationDto {

    @NotBlank
    @Size(min = 3, max = 20, message = "{user.username.size}")
    @UniqueUsername(message = "{user.username.unique}")
    private String username;

    @NotBlank
    @Size(min = 3, max = 20, message = "{user.full-name.size}")
    private String fullName;

    @NotNull
    @Past
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @MinAge
    private Date birthDate;

    @NotBlank
    @Email
    @UniqueEmail(message = "{user.email.unique}")
    private String email;

    @NotBlank
    @Size(min = 4, max = 20, message = "{user.password.size}")
    private String password;

    private String confirmPassword;

    @Override
    public String toString() {
        return "UserRegisterDto{" +
                "userName='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", birthDate=" + birthDate +
                ", email='" + email + '\'' +
                ", password='" + (password != null ? "[PROVIDED]" : null) + '\'' +
                ", confirmPassword='" + (confirmPassword != null ? "[PROVIDED]" : null) + '\'' +
                '}';
    }
}
