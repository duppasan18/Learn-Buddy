package com.pasan.config.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {

    // 小程序id
    private String appId;
    // 小程序密钥
    private String secret;

}
