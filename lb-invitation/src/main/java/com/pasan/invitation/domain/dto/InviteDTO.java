package com.pasan.invitation.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteDTO {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 房间id
     */
    private Long roomId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 最大人数
     */
    private Integer maxMembers;
}
