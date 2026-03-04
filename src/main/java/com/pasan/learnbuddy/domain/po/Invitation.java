package com.pasan.learnbuddy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name="Invitation对象", description="学习邀约表")
public class Invitation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "邀约id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "发起人id")
    private Integer initiatorId;

    @Schema(description = "限制人数")
    private Integer maxNumber;

    @Schema(description = "发起时间")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "地点id")
    private Integer locationId;

    @Schema(description = "已参加人数")
    private Integer attendNum;


}
