package com.pasan.learnbuddy.config.jjwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    /**
     * 过期时间(单位:秒)
     */
    private int ACCESS_EXPIRE;
    /**
     * 私钥 / 生成签名的时候使用的秘钥secret，一般可以从本地配置文件中读取，切记这个秘钥不能外露，只在服务端使用，在任何场景都不应该流露出去。
     * 一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。
     * 应该大于等于 256位(长度32及以上的字符串)，并且是随机的字符串
     */
    private String SECRET;
    /**
     * jwt签发者
     */
    private String JWT_ISS;
    /**
     * jwt主题
     */
    private String SUBJECT;



}
