package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserForm {
    @NotNull
    @Size(max = 255, message = "Email cannot be more than 255 characters")
    @Email(message = "Please enter an email")
    private String email;

    @NotNull
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,30}$",
            message = "Password must be 8-30 characters long and include: 1 uppercase, 1 lowercase, 1 digit, and 1 special character (@#$%^&+=)."
    )
    private String password;
}
