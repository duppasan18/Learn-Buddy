package com.pasan.config.jjwt;

import com.pasan.config.security.MySecurityProperties;
import com.pasan.filter.JwtAuthenticationFilter;
import com.pasan.util.JwtUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil(JwtProperties properties){
        return new JwtUtil(properties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, MySecurityProperties properties, StringRedisTemplate redisTemplate){
        return new JwtAuthenticationFilter(jwtUtil, properties,redisTemplate);
    }

}
