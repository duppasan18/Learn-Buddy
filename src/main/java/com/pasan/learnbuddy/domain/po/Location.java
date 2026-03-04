package com.pasan.learnbuddy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 自习地点表
 * </p>
 *
 * @author pasan
 * @since 2026-03-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("location")
@Schema(name="Location对象", description="自习地点表")
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "地点id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "地点名称")
    private String locationName;

    @Schema(description = "地点类型 1-自习室 2-图书馆")
    private Integer type;

    @Schema(description = "最大容纳人数")
    private Integer maxNumber;

    @Schema(description = "已容纳人数")
    private Integer capacity;


}
