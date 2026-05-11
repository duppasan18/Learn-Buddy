package com.pasan.location.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RoomStatus {

    DISABLE(0, "不可用"),
    ENABLE(1, "可用");

    @EnumValue
    private final Integer code;
    private final String desc;

    RoomStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RoomStatus fromCode(Integer code) {
        for (RoomStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
