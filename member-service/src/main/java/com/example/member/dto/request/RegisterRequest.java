package com.example.member.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class RegisterRequest {
    @JsonAlias("username")  // signup.html에서 username으로 보냄
    private String name;
    private String userId;
    private String password;
    private String passwordConfirm;
    private String email;
    private String verificationCode;
}
