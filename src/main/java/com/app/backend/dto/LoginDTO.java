package com.app.backend.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String password;
}