package com.pasan.location.controller;

import com.pasan.location.domain.dto.Location;
import com.pasan.location.domain.po.Room;
import com.pasan.location.domain.vo.NearByRoomVO;
import com.pasan.location.domain.vo.RoomVO;
import com.pasan.location.service.ILocationService;
import com.pasan.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final ILocationService locationService;

    /**
     * 保存当前用户的位置（经纬度）到Redis GEO
     */
    @PostMapping("/saveUserLocation")
    public Result saveUserLocation(@RequestBody Location dto){
        locationService.saveUserLocation(dto);
        return Result.success();
    }

    /**
     * 查询附近的自习室列表
     */
    @GetMapping("/nearbyRoom")
    public Result<List<NearByRoomVO>> nearbyRoom(Location dto){
        return Result.success(locationService.getNearbyRoom(dto));
    }

    /**
     * 添加自习室信息并缓存到Redis
     */
    @PostMapping("/addRoom")
    public Result addRoom(@RequestBody Room room){
        locationService.addRoom(room);
        return Result.success();
    }

    /**
     * 获取指定自习室的经纬度坐标
     */
    @GetMapping("/roomLocation/{roomId}")
    public Location getRoomLocation(@PathVariable Long roomId){
        return locationService.getRoomLocation(roomId);
    }

    /**
     * 查询附近正在进行的邀约ID列表
     */
    @GetMapping("/nearbyRoomInvite")
    public List<Long> nearbyRoomInvite(){
        return locationService.getNearbyRoomInvite();
    }

    /**
     * 批量获取自习室信息
     */
    @GetMapping("/roomInfos")
    public List<RoomVO> getRoomInfos(@RequestParam("ids") List<Long> ids){
        return locationService.getRoomInfos(ids);
    }


}
