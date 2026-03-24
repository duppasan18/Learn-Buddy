package com.pasan.location.controller;

import com.pasan.location.domain.dto.LocationDTO;
import com.pasan.location.domain.po.Room;
import com.pasan.location.domain.vo.NearByRoomVO;
import com.pasan.location.domain.vo.NearByUserVO;
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
    @PostMapping("/save")
    public Result saveLocation(@RequestBody LocationDTO dto){
        locationService.saveLocation(dto);
        return Result.success();
    }

    /**
     * 获取附近的人
     */
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
     * 获取附近的自习空间
     */
    @GetMapping("/nearbyRoom")
    public Result<List<NearByRoomVO>> nearbyRoom(LocationDTO dto){
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


}
