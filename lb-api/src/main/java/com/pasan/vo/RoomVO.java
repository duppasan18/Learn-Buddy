package com.pasan.vo;

import com.pasan.enums.RoomStatus;
import lombok.Data;

@Data
public class RoomVO {
    private Long id;

    //地点名称
    private String name;

    //地点类型 1-自习室 2-图书馆
    private Integer type;

    //容纳人数
    private Integer capacity;

    //状态 0-不可用 1-可用
    private RoomStatus status;
}
