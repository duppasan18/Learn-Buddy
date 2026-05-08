package com.pasan.invitation.service;

import com.pasan.invitation.domain.dto.StudyStartDTO;
import com.pasan.invitation.domain.vo.StudyStatusVO;

import java.util.Map;

public interface IStudyRecordService {
    void startStudy(StudyStartDTO dto);
    void stopStudy();
    StudyStatusVO getStatus();
    int getTodayDuration();
    Map<String, Double> getRanking(int topN);
}
