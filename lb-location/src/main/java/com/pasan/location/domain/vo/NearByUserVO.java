package com.pasan.location.domain.vo;

import com.pasan.enums.Gender;
import lombok.Data;

@Data
public class NearByUserVO {

    private Long id;

    //姓名
    private String name;

    //性别 0-男 1-女 2-保密
    private Gender gender;

    //头像
    private String avatar;

    // 距离,单位公里
    private Double distance;

}
