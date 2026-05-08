package com.pasan.invitation.controller;

import com.pasan.invitation.domain.po.ChatMessage;
import com.pasan.invitation.service.IChatMessageService;
import com.pasan.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatMessageService chatMessageService;

    /**
     * 查询指定邀约的聊天历史记录
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> getHistory(@RequestParam Long inviteId,
                                                @RequestParam(defaultValue = "50") int limit) {
        return Result.success(chatMessageService.getHistory(inviteId, limit));
    }

    /**
     * 保存聊天消息（供WebSocket服务端调用）
     */
    @PostMapping("/message")
    public Result saveMessage(@RequestBody ChatMessage message) {
        chatMessageService.saveMessage(message);
        return Result.success("ok");
    }
}
