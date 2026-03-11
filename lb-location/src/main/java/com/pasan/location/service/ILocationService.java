package com.pasan.location.service;

import com.pasan.location.domain.dto.LocationDTO;
import com.pasan.location.domain.vo.NearByUserVO;

import java.util.List;

public interface ILocationService {
    void saveLocation(LocationDTO dto);

    List<NearByUserVO> getNearby();
}
