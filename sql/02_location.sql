-- 自习室表（lb_location 库）
CREATE TABLE IF NOT EXISTS `room` (
    id        BIGINT PRIMARY KEY COMMENT '自习室id',
    name      VARCHAR(50)  NOT NULL COMMENT '名称',
    type      TINYINT      COMMENT '类型 1自习室 2图书馆',
    capacity  INT          COMMENT '可容纳人数',
    longitude DOUBLE       COMMENT '经度',
    latitude  DOUBLE       COMMENT '纬度',
    status    TINYINT      DEFAULT 0 COMMENT '状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习室表';
