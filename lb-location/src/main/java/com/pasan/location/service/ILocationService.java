package com.pasan.location.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pasan.location.domain.dto.Location;
import com.pasan.location.domain.po.Room;
import com.pasan.location.domain.vo.NearByRoomVO;
import com.pasan.location.domain.vo.NearByUserVO;
import com.pasan.location.domain.vo.RoomVO;
import com.pasan.vo.UserInfoVO;

import java.util.List;

public interface ILocationService extends IService<Room> {
    void saveUserLocation(Location dto);

    List<NearByUserVO> getNearbyUser();

    void deleteLocation();

    List<NearByRoomVO> getNearbyRoom(Location dto);

    void addRoom(Room room);

    List<Long> getNearbyRoomInvite();

    Location getRoomLocation(Long roomId);

    List<RoomVO> getRoomInfos(List<Long> ids);
}
