package com.pasan.learnbuddy.domain.enums;


import lombok.Getter;

@Getter
public enum Gender {

    MALE(0, "男"),
    FEMALE(1, "女"),
    SECRET(2, "保密");

    private final Integer code;
    private final String desc;

    Gender(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Gender fromCode(Integer code) {
        for (Gender gender : values()) {
            if (gender.code.equals(code)) {
                return gender;
            }
        }
        return null;
    }
}
