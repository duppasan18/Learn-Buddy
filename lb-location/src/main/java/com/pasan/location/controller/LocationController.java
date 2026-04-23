package com.pasan.location.controller;

import com.pasan.location.domain.dto.Location;
import com.pasan.location.domain.po.Room;
import com.pasan.location.domain.vo.NearByRoomVO;
import com.pasan.location.domain.vo.NearByUserVO;
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
     * 接收用户位置信息并存储到redis中
     */
    @PostMapping("/saveUserLocation")
    public Result saveUserLocation(@RequestBody Location dto){
        locationService.saveUserLocation(dto);
        return Result.success();
    }

    /**
     * 获取附近的人
     */
    // todo 解耦，应该由用户模块返回用户信息
    @GetMapping("/nearbyUser")
    public Result<List<NearByUserVO>> nearbyUser(){
        List<NearByUserVO> nearbyUser = locationService.getNearbyUser();
        return Result.success(nearbyUser);
    }

    /**
     * 删除用户位置信息
     */
    @DeleteMapping("/delete")
    public Result deleteLocation(){
        locationService.deleteLocation();
        return Result.success();
    }

    /**
     * 获取附近的自习空间，用于创建自习邀约，或者自由自习
     */
    @GetMapping("/nearbyRoom")
    public Result<List<NearByRoomVO>> nearbyRoom(Location dto){
        List<NearByRoomVO> nearbyRoom = locationService.getNearbyRoom(dto);
        return Result.success(nearbyRoom);
    }

    /**
     * 添加自习空间信息
     */
    @PostMapping("/addRoom")
    public Result addRoom(@RequestBody Room room){
        locationService.addRoom(room);
        return Result.success();
    }

    /**
     * 获取指定自习空间信息
     */
    @GetMapping("/roomLocation/{roomId}")
    public Location getRoomLocation(@PathVariable Long roomId){
        return locationService.getRoomLocation(roomId);
    }

    /**
     * 获取附近正在进行的自习邀约id
     */
    @GetMapping("/nearbyRoomInvite")
    public List<Long> nearbyRoomInvite(){
        return locationService.getNearbyRoomInvite();
    }

    /**
     * 根据自习室id集合获取信息
     */
    @GetMapping("/roomInfos")
    public List<RoomVO> getRoomInfos(@RequestParam("ids") List<Long> ids){
        return locationService.getRoomInfos(ids);
    }


}
