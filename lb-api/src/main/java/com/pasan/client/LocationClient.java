package com.pasan.client;

import com.pasan.dto.Location;
import com.pasan.vo.RoomVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("lb-location")
public interface LocationClient {

    @GetMapping("/location/roomLocation/{roomId}")
    Location getRoomLocation(@PathVariable Long roomId);

    @GetMapping("/location/nearbyRoomInvite")
    List<Long> nearbyRoomInvite();

    @GetMapping("/location/roomInfos")
    List<RoomVO> getRoomInfos(@RequestParam("ids") List<Long> ids);
}
