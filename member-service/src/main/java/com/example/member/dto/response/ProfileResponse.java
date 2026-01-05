package com.example.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String userType;
    private String provider;
    private String createdAt;
}
