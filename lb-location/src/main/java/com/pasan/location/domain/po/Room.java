package com.pasan.location.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 自习地点表
 * </p>
 *
 * @author pasan
 * @since 2026-03-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("room")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    //地点id
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    //地点名称
    private String name;

    //地点类型 1-自习室 2-图书馆
    private Integer type;

    //容纳人数
    private Integer capacity;

    //经度
    private Double longitude;

    //纬度
    private Double latitude;


}
