package com.pasan.learnbuddy.util;

import com.pasan.learnbuddy.config.jjwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenAccessToken() {
        String accessToken = jwtUtil.genAccessToken("pasan");
        log.info("token是: {}", accessToken);

        Jws<Claims> claimsJws = jwtUtil.parseClaim(accessToken);
        log.info("claimsJws: {}", claimsJws);

        Object username = claimsJws.getPayload().get("username");
        log.info("username: {}", username);

    }
  
}