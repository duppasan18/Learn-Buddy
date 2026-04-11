package com.pasan.location.config;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pasan.constants.RedisConstant;
import com.pasan.location.domain.enums.RoomStatus;
import com.pasan.location.domain.po.Room;
import com.pasan.location.mapper.LocationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationDataLoader implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;
    private final LocationMapper locationMapper;


    @Override
    public void run(String... args) throws Exception {
        loadRoomData2Redis();
    }

    private void loadRoomData2Redis() {
        log.info("正在初始化地点信息");
        List<Room> roomList = locationMapper.selectList(new LambdaQueryWrapper<>());
        if(redisTemplate.hasKey(RedisConstant.ROOM_INFO_KEY) && redisTemplate.hasKey(RedisConstant.ROOM_LOCATION_KEY)){
            log.info("地点信息已存在");
            return;
        }
        for(Room room : roomList){
            // 缓存到Redis中
            Integer id = room.getId();
            String key = RedisConstant.ROOM_LOCATION_KEY+id;
            redisTemplate.execute(new SessionCallback<Object>() {
                @Nullable
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    operations.multi(); // 开始事务
                    // 存储经纬度
                    operations.opsForGeo().add(RedisConstant.ROOM_LOCATION_KEY,
                            new Point(room.getLongitude(), room.getLatitude()), key);
                    // 存储信息
                    operations.opsForHash().put(RedisConstant.ROOM_INFO_KEY, id.toString(), JSON.toJSONString(room));
                    return operations.exec(); // 提交事务
                }
            });
        }
        log.info("地点信息初始化完成");
    }
}
