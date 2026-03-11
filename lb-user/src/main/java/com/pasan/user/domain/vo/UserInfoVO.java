package com.pasan.user.domain.vo;

import com.pasan.user.domain.enums.Gender;
import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;

    //姓名
    private String name;

    //性别 0-男 1-女 2-保密
    private Gender gender;

    //头像
    private String avatar;
}
