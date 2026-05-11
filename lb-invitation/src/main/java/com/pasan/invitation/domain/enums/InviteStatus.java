package com.pasan.invitation.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum InviteStatus {
    PROCESSING(0, "进行中"),
    FINISHED(1, "已结束"),
    CANCELED(2, "已取消");

    @EnumValue
    private final Integer code;
    private final String desc;

    InviteStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
