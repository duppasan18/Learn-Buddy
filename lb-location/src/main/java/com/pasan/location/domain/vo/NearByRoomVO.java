package com.pasan.location.domain.vo;

import lombok.Data;

@Data
public class NearByRoomVO {

    private Long id;

    //名称
    private String name;

    //类型 1-自习室 2-图书馆
    private Integer type;

    // 距离,单位跟随service层，m
    private Double distance;

}
