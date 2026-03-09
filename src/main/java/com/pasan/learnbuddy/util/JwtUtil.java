package com.pasan.learnbuddy.util;

import com.pasan.learnbuddy.config.jjwt.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    /**
     * 加密算法
     */
    private final static SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;
    /**
     * 秘钥实例
     */
    private SecretKey KEY;
    private String issuer;
    private String subject;
    private long expireSeconds;

    public JwtUtil(JwtProperties properties){
        this.KEY = Keys.hmacShaKeyFor(properties.getSECRET().getBytes());
        this.issuer = properties.getJWT_ISS();
        this.subject = properties.getSUBJECT();
        this.expireSeconds = properties.getACCESS_EXPIRE();
    }

    /*
    这些是一组预定义的声明，它们 不是强制性的，而是推荐的 ，以 提供一组有用的、可互操作的声明 。
    iss: jwt签发者
    sub: jwt所面向的用户
    aud: 接收jwt的一方
    exp: jwt的过期时间，这个过期时间必须要大于签发时间
    nbf: 定义在什么时间之前，该jwt都是不可用的.
    iat: jwt的签发时间
    jti: jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击
     */
    public String createJWT(Map<String, Object> claims) {
        // 令牌id
        String uuid = UUID.randomUUID().toString();
        Date exprireDate = Date.from(Instant.now().plusSeconds(expireSeconds));

        return Jwts.builder()
                // 设置头部信息header
                .header()
                .and()
                // 设置自定义负载信息payload
                .claims(claims)
                // 令牌ID
                .id(uuid)
                // 过期日期
                .expiration(exprireDate)
                // 签发时间
                .issuedAt(new Date())
                // 主题
                .subject(subject)
                // 签发者
                .issuer(issuer)
                // 签名
                .signWith(KEY, ALGORITHM)
                .compact();
    }
    /**
     * 解析token
     * @param token token
     * @return Jws<Claims>
     */
    public Jws<Claims> parseClaim(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token);
    }

    public JwsHeader parseHeader(String token) {
        return parseClaim(token).getHeader();
    }

    public Claims parsePayload(String token) {
        try {
            return parseClaim(token).getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("token非法或已过期");
        }
    }


}
