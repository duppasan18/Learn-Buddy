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
 * 
 * </p>
 *
 * @author pasan
 * @since 2026-03-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Schema(name="User对象", description="")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名，不可为中文")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "总学习时长，单位：min")
    private Integer learnedTime;

    @Schema(description = "用户id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


}
