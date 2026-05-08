package com.pasan.invitation.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudyStatusVO {
    private boolean studying;
    private Long recordId;
    private String subject;
    private LocalDateTime startTime;
    private Long durationSec;
}
