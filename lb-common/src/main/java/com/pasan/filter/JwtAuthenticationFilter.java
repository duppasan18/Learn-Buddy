package com.pasan.filter;


import com.pasan.config.security.MySecurityProperties;
import com.pasan.constants.RedisConstant;
import com.pasan.exception.LoginFailedException;
import com.pasan.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * jwt验证过滤器
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private final Set<String> whiteList;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, MySecurityProperties properties, StringRedisTemplate redisTemplate) {
        this.whiteList = properties.getWhiteList();
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        // 排除白名单路径
        if (whiteList.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        log.info("authHeader: {}", authHeader);

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("token无效或缺失，请先登录");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.parsePayload(token);
            Long userId = claims.get("userId", Long.class);

            log.info("userId: {}", userId);

            String validToken = redisTemplate.opsForValue().get(RedisConstant.TOKEN_KEY + userId);
            if(validToken == null || !validToken.equals(token)){
                throw new LoginFailedException("token已失效");
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.emptyList()
            );
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {
            log.error("无法解析token", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"msg\":\""+e.getMessage()+"\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
