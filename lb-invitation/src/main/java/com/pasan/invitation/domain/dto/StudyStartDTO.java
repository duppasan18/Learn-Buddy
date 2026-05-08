package com.pasan.invitation.domain.dto;

import lombok.Data;

@Data
public class StudyStartDTO {
    private String subject;
    private Long roomId;
    private Long inviteId;
}
