package com.pasan.location.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pasan.client.UserClient;
import com.pasan.constants.RedisConstant;
import com.pasan.exception.BusinessException;
import com.pasan.exception.LoginFailedException;
import com.pasan.location.domain.dto.Location;
import com.pasan.location.domain.po.Room;
import com.pasan.location.domain.vo.NearByRoomVO;
import com.pasan.location.domain.vo.NearByUserVO;
import com.pasan.location.domain.vo.RoomVO;
import com.pasan.location.mapper.LocationMapper;
import com.pasan.location.service.ILocationService;
import com.pasan.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Room> implements ILocationService {

    private final StringRedisTemplate redisTemplate;

    private final UserClient userclient;

    /**
     * 保存位置信息到redis中
     * @param dto
     */
    @Override
    public void saveUserLocation(Location dto) {
        // 获取用户id
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userId == null){
            throw new LoginFailedException("用户未登录");
        }
        // 存储位置信息
        String member = userId.toString();
        redisTemplate.opsForGeo().add(RedisConstant.USER_LOCATION_KEY,
                new Point(dto.getLongitude(), dto.getLatitude()), member);

    }

    @Override
    public List<NearByUserVO> getNearbyUser() {
        // 获取用户id
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 组装查询数据
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()     // 返回距离
                .sortAscending();      // 距离从小到大排序
        String member = RedisConstant.NEARBY_MEMBER_PREFIX+userId;
        Distance distanceScope = new Distance(0.5, Metrics.KILOMETERS); //搜索附近0.5公里
        // 获取附近用户的id
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(RedisConstant.USER_LOCATION_KEY, member, distanceScope,args);
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
        List<Long> ids = new ArrayList<>();
        for (String idStr : distanceMap.keySet()) {
            ids.add(Long.valueOf(idStr));
        }
        log.info("附近的人ids:{}",ids);
        // 根据id查询用户信息返回
        List<UserInfoVO> userInfos = userclient.getUserInfos(ids);
        Map<Long, UserInfoVO> userMap = userInfos.stream()
                .collect(Collectors.toMap(UserInfoVO::getId, vo -> vo));

        List<NearByUserVO> list = new ArrayList<>(userInfos.size());
        for (Long id : ids) {
            UserInfoVO user = userMap.get(id);
            if(user!=null){
                NearByUserVO vo = BeanUtil.copyProperties(user, NearByUserVO.class);
                vo.setDistance(distanceMap.get(id.toString()));
                list.add(vo);
            }
        }
        return list;
    }

    @Override
    public void deleteLocation() {
        // 获取用户id
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userId == null){
            throw new LoginFailedException("用户未登录");
        }
        // 存储位置信息
        String member = RedisConstant.NEARBY_MEMBER_PREFIX+userId;
        redisTemplate.opsForGeo().remove(RedisConstant.USER_LOCATION_KEY, member);
    }

    @Override
    public List<NearByRoomVO> getNearbyRoom(Location dto) {
        // 组装查询数据
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()     // 返回距离
                .sortAscending();      // 距离从小到大排序
        Circle circle = new Circle(dto.getLongitude(), dto.getLatitude(), 50000); // 默认单位为m
        // todo 判key是否存在，空则重新初始化
        // 按照距离获取最近的自习空间
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(RedisConstant.ROOM_LOCATION_KEY,circle,args);
        if (results == null) {
            return List.of();
        }
        LinkedHashMap<String, Double> distanceMap = results.getContent().stream()                 // 提取 GeoLocation<String>
                .collect(Collectors.toMap(
                        r -> r.getContent().getName().replace(RedisConstant.ROOM_LOCATION_KEY, ""), // key: 去掉前缀
                        r -> r.getDistance().getValue(),                                   // value: 距离
                        (oldVal, newVal) -> oldVal,                                        // 合并策略（一般不会重复）
                        LinkedHashMap::new                                                   // 保持顺序
                ));
        Set<String> ids = distanceMap.keySet();
        log.info("附近自习室的ids:{}",ids);
        List<Room> rooms = redisTemplate.opsForHash().multiGet(RedisConstant.ROOM_INFO_KEY, new ArrayList<>(ids))
                .stream()
                .filter(Objects::nonNull)
                .map(o -> {
                    String json = (String) o;
                    return JSON.parseObject(json, Room.class);
                })
                .toList();
        List<NearByRoomVO> vos = BeanUtil.copyToList(rooms, NearByRoomVO.class);
        for (NearByRoomVO vo : vos) {
            Double distance = distanceMap.get(vo.getId().toString());
            if(distance != null) {
                vo.setDistance(distance);
            }
        }
        // todo 获取容纳人数并添加到VO中
        return vos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoom(Room room) {
        try {
            // 持久化到MySql中
            boolean success = save(room);
            if(!success){
                throw new BusinessException("添加自习室失败");
            }
            // 缓存到Redis中
            Integer id = room.getId();
            String member = id.toString();
            redisTemplate.execute(new SessionCallback<Object>() {
                @Nullable
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    operations.multi(); // 开始事务
                    // 存储经纬度
                    operations.opsForGeo().add(RedisConstant.ROOM_LOCATION_KEY,
                            new Point(room.getLongitude(), room.getLatitude()), member);
                    // 存储信息
                    operations.opsForHash().put(RedisConstant.ROOM_INFO_KEY, id.toString(), JSON.toJSONString(room));
                    return operations.exec(); // 提交事务
                }
            });
        } catch (Exception e){
            log.error("添加自习室失败",e);
            throw new BusinessException("添加自习室失败");
        }
    }

    @Override
    public List<Long> getNearbyRoomInvite() {
        // 获取用户id
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userId == null){
            throw new LoginFailedException("用户未登录");
        }
        // 获取用户位置信息
        List<Point> position = redisTemplate.opsForGeo().position(RedisConstant.USER_LOCATION_KEY, userId.toString());
        if(position == null || position.isEmpty()){
            return List.of();
        }
        Point point = position.get(0);
        Location dto = new Location();
        dto.setLatitude(point.getY());
        dto.setLongitude(point.getX());

        // 组装查询数据
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()     // 返回距离
                .sortAscending();      // 距离从小到大排序
        Circle circle = new Circle(dto.getLongitude(), dto.getLatitude(), 50000); // 默认单位为m
        // 按照距离获取最近的自习邀约
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(RedisConstant.INVITATION_LOCATION_KEY,circle,args);
        if (results == null) {
            return List.of();
        }
        LinkedHashMap<String, Double> distanceMap = results.getContent().stream()                 // 提取 GeoLocation<String>
                .collect(Collectors.toMap(
                        r -> r.getContent().getName().replace(RedisConstant.INVITATION_LOCATION_KEY, ""), // key: 去掉前缀
                        r -> r.getDistance().getValue(),                                   // value: 距离
                        (oldVal, newVal) -> oldVal,                                        // 合并策略（一般不会重复）
                        LinkedHashMap::new                                                   // 保持顺序
                ));
        Set<String> ids = distanceMap.keySet();
        log.info("附近自习邀约的ids:{}",ids);
        return ids.stream().map(Long::parseLong).toList();
    }

    @Override
    public Location getRoomLocation(Long roomId) {
        List<Point> position = redisTemplate.opsForGeo().position(RedisConstant.ROOM_LOCATION_KEY, roomId.toString());
        if(CollUtil.isEmpty( position)){
            throw new BusinessException("未查询到对应自习室信息");
        }
        Point point = position.get(0);
        Location location = new Location();
        location.setLongitude(point.getY());
        location.setLatitude(point.getX());
        return location;
    }

    @Override
    public List<RoomVO> getRoomInfos(List<Long> ids) {
        List<String> fields = ids.stream()
                .map(String::valueOf)
                .toList();

        List<Object> objectList = redisTemplate.opsForHash().multiGet(RedisConstant.ROOM_INFO_KEY, Arrays.asList(fields.toArray()));
        List<RoomVO> list = new ArrayList<>(objectList.size());
        for (Object o : objectList) {
            String json = (String) o;
            Room room = JSON.parseObject(json,Room.class);
            RoomVO vo = BeanUtil.copyProperties(room, RoomVO.class);
            list.add(vo);
        }
        return list;
    }
}
