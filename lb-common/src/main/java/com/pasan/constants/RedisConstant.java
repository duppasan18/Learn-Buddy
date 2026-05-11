package com.pasan.constants;

public class RedisConstant {
    public static final String USER_LOCATION_KEY = "user:geo"; // 用户位置
    public static final String USER_PREFIX = "user:";  // 用户前缀
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:" ; // token前缀

    public static final String ROOM_LOCATION_KEY = "room:geo"; // 自习室位置
    public static final String ROOM_INFO_KEY_PREFIX = "room:info"; // 自习室信息

    public static final String INVITATION_COUNT_KEY_PREFIX = "invite:count:"; // 邀约当前人数
    public static final String INVITATION_LOCATION_KEY = "invite:geo"; // 邀约自习室位置
    public static final String INVITATION_INFO_KEY_PREFIX = "invite:info:"; // 邀约信息
    public static final String INVITATION_LOCK_KEY_PREFIX = "invite:lock:";

    public static final String STUDY_ACTIVE_KEY_PREFIX = "study:active:"; // 当前学习状态缓存
    public static final String STUDY_RANKING_KEY = "study:ranking"; // 学习时长排行榜 ZSET
}
