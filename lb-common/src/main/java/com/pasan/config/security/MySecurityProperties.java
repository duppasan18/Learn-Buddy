package com.pasan.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@ConfigurationProperties(prefix = "security")
@Data
public class MySecurityProperties {
    // 白名单
    private Set<String> whiteList;
}
