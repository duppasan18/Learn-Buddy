package com.pasan.location.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.pasan.client.UserClient;
import com.pasan.constants.RedisConstant;
import com.pasan.exception.LoginFailedException;
import com.pasan.location.domain.dto.LocationDTO;
import com.pasan.location.domain.vo.NearByUserVO;
import com.pasan.location.service.ILocationService;
import com.pasan.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements ILocationService {

    private final StringRedisTemplate redisTemplate;

    private final UserClient userclient;

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

    @Override
    public List<NearByUserVO> getNearby() {
        // 获取用户id
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 组装查询数据
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()     // 返回距离
                .sortAscending();      // 距离从小到大排序
        String member = RedisConstant.NEARBY_MEMBER_PREFIX+userId;
        Distance distanceScope = new Distance(0.5, Metrics.KILOMETERS);
        // 获取附近用户的id
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(RedisConstant.NEARBY_KEY, member, distanceScope,args);
        if (results == null) {
            return List.of();
        }
        LinkedHashMap<String, Double> distanceMap = results.getContent().stream()                 // 提取 GeoLocation<String>
                .filter(r -> !r.getContent().getName().equals(member))            // 排除自己
                .collect(Collectors.toMap(
                        r -> r.getContent().getName().replace(RedisConstant.NEARBY_MEMBER_PREFIX, ""), // key: 去掉前缀
                        r -> r.getDistance().getValue(),                                   // value: 距离
                        (oldVal, newVal) -> oldVal,                                        // 合并策略（一般不会重复）
                        LinkedHashMap::new                                                   // 保持顺序
                ));
        Set<Long> ids = distanceMap.keySet().stream()
                .map(Long::valueOf) // 将key转换为Long
                .collect(Collectors.toSet());

        log.info("附近的人ids:{}",ids);
        // 根据id查询用户信息返回
        List<UserInfoVO> userInfos = userclient.getUserInfos(ids);

        List<NearByUserVO> list = new ArrayList<>(userInfos.size());
        for (UserInfoVO user : userInfos) {
            NearByUserVO vo = BeanUtil.copyProperties(user, NearByUserVO.class);
            Double distance = distanceMap.get(vo.getId().toString());
            if(distance ==  null){
                continue;
            }
            vo.setDistance(distance);
            list.add(vo);
        }

        return list;
    }
}
