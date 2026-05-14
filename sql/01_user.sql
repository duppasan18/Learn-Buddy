-- 用户表（lb_user 库）
CREATE TABLE IF NOT EXISTS `user` (
    id          BIGINT PRIMARY KEY COMMENT '用户id',
    openid      VARCHAR(50)  NOT NULL COMMENT '微信用户唯一标识',
    name        VARCHAR(20)  COMMENT '用户名',
    gender      TINYINT      COMMENT '性别 0男 1女 2保密',
    avatar      VARCHAR(500) COMMENT '头像URL',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
