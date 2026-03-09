package com.pasan.learnbuddy.config.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
@Data
public class WechatProperties {

    // 小程序id
    private String appId;
    // 小程序密钥
    private String secret;

}
