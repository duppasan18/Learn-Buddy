package com.pasan.invitation.controller;

import com.pasan.invitation.domain.dto.AttendInviteDTO;
import com.pasan.invitation.domain.dto.InviteDTO;
import com.pasan.invitation.domain.vo.InviteVO;
import com.pasan.invitation.service.IInviteService;
import com.pasan.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {

    private final IInviteService inviteService;

    /** 发布自习邀约 */
    @PostMapping("/create")
    public Result createInvite(@RequestBody InviteDTO dto) {
        inviteService.createInvite(dto);
        return Result.success("发布成功");
    }

    /** 获取附近正在进行的邀约列表 */
    @GetMapping("/nearby")
    public Result<List<InviteVO>> getNearbyInvite() {
        return Result.success(inviteService.getNearbyInvite());
    }

    /** 参与自习邀约 */
    @PostMapping("/join")
    public Result joinInvite(@RequestBody AttendInviteDTO dto) {
        inviteService.joinInvite(dto);
        return Result.success("参与成功");
    }

    /** 查看邀约详情 */
    @GetMapping("/detail")
    public Result<InviteVO> getInviteDetail(@RequestParam Long inviteId) {
        return Result.success(inviteService.getInviteDetail(inviteId));
    }

    /** 延长邀约结束时间（每次30分钟） */
    @PostMapping("/extend")
    public Result extendInvite(@RequestBody Map<String, Long> body) {
        inviteService.extendInvite(body.get("inviteId"));
        return Result.success("已延长30分钟");
    }

    /** 退出邀约 */
    @PostMapping("/leave")
    public Result leaveInvite(@RequestBody Map<String, Long> body) {
        inviteService.leaveInvite(body.get("inviteId"));
        return Result.success("已退出邀约");
    }

    /** 解散邀约（仅发布者） */
    @PostMapping("/cancel")
    public Result cancelInvite(@RequestBody Map<String, Long> body) {
        inviteService.cancelInvite(body.get("inviteId"));
        return Result.success("已解散邀约");
    }

    /** 查询当前用户参与的进行中邀约 */
    @GetMapping("/my-active")
    public Result<InviteVO> getMyActiveInvite() {
        return Result.success(inviteService.getMyActiveInvite());
    }
}
