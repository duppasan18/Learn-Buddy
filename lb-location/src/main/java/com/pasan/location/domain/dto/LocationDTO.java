package com.pasan.location.domain.dto;

import lombok.Data;

/**
 * 位置信息dto
 */
@Data
public class LocationDTO {

    // 纬度
    private Double latitude;
    // 经度
    private Double longitude;

}
