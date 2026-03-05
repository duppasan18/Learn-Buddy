package com.pasan.learnbuddy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 学习邀约表
 * </p>
 *
 * @author pasan
 * @since 2026-03-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("invitation")
public class Invitation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer initiatorId;

    private Integer maxNumber;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Integer locationId;

    private Integer attendNum;


}
