package com.pasan.location.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pasan.location.domain.dto.LocationDTO;
import com.pasan.location.domain.po.Room;
import com.pasan.location.domain.vo.NearByRoomVO;
import com.pasan.location.domain.vo.NearByUserVO;

import java.util.List;

public interface ILocationService extends IService<Room> {
    void saveLocation(LocationDTO dto);

    List<NearByUserVO> getNearbyUser();

    void deleteLocation();

    List<NearByRoomVO> getNearbyRoom(LocationDTO dto);

    void addRoom(Room room);
}
