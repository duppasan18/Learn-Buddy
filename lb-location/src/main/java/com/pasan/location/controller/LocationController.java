package com.pasan.location.controller;

import com.pasan.location.domain.dto.LocationDTO;
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
     * 接收位置信息并存储到redis中
     */
    @PostMapping("/save")
    public Result saveLocation(@RequestBody LocationDTO dto){
        locationService.saveLocation(dto);
        return Result.success();
    }

    /**
     * 获取附近的人
     */
    @GetMapping("/nearby")
    public Result<List<NearByUserVO>> nearby(){
        List<NearByUserVO> nearby = locationService.getNearby();
        return Result.success(nearby);
    }


}
