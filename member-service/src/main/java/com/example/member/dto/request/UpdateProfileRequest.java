package com.example.member.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String currentPassword;
    private String newPassword;
    private String phone;
}
