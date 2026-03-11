package com.pasan.config.wechat;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WechatProperties.class)
@ConditionalOnProperty(prefix = "wechat", name = "enabled", havingValue = "true")
public class WechatConfig {


}
