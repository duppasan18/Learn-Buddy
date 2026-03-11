package com.pasan.location.service.impl;

import com.pasan.constants.RedisConstant;
import com.pasan.exception.BusinessException;
import com.pasan.exception.LoginFailedException;
import com.pasan.location.domain.dto.LocationDTO;
import com.pasan.location.service.ILocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements ILocationService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 保存位置信息到redis中
     * @param dto
     */
    @Override
    public void saveLocation(LocationDTO dto) {
        // 获取用户id
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userId == null){
            throw new LoginFailedException("用户未登录");
        }
        // 存储位置信息
        String member = RedisConstant.NEARBY_MEMBER_PREFIX+userId;
        redisTemplate.opsForGeo().add(RedisConstant.NEARBY_KEY,
                new Point(dto.getLongitude(), dto.getLatitude()), member);

    }
}
