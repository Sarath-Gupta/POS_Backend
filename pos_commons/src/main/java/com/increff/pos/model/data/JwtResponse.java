package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String role;

    public JwtResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }
}
