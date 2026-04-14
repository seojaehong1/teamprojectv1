package com.example.product.dto.system;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SystemInfoDto {
    private String serviceName;
    private String version;
    private String javaVersion;
    private String springBootVersion;
    private String buildTime;
    private String activeProfile;
    private int serverPort;
}