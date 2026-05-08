package com.pasan.invitation.controller;

import com.pasan.invitation.domain.dto.StudyStartDTO;
import com.pasan.invitation.domain.vo.StudyStatusVO;
import com.pasan.invitation.service.IStudyRecordService;
import com.pasan.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final IStudyRecordService studyRecordService;

    /** 开始自习计时 */
    @PostMapping("/start")
    public Result startStudy(@RequestBody StudyStartDTO dto) {
        studyRecordService.startStudy(dto);
        return Result.success("开始学习");
    }

    /** 停止自习计时，结算时长写入排行榜 */
    @PostMapping("/stop")
    public Result stopStudy() {
        studyRecordService.stopStudy();
        return Result.success("已停止学习");
    }

    /** 查询当前学习状态与已计时长 */
    @GetMapping("/status")
    public Result<StudyStatusVO> getStatus() {
        return Result.success(studyRecordService.getStatus());
    }

    /** 查询今日累计学习时长（分钟） */
    @GetMapping("/today")
    public Result<Map<String, Integer>> getTodayDuration() {
        return Result.success(Map.of("durationMin", studyRecordService.getTodayDuration()));
    }

    /** 查询学习时长排行榜前N名 */
    @GetMapping("/ranking")
    public Result<Map<String, Double>> getRanking(@RequestParam(defaultValue = "10") int topN) {
        return Result.success(studyRecordService.getRanking(topN));
    }
}
