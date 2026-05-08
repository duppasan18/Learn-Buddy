package com.pasan.invitation.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteVO {

    private Long userId;

    /**
     * 邀约id
     */
    private Long id;


    /**
     * 发布用户名称
     */
    private String userName;

    /**
     * 邀约所在自习室
     */
    private String roomName;

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

    /**
     * 已参加人数
     */
    private Integer joinMembers;
}
