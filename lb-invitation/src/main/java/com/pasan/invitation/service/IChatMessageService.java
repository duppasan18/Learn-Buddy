package com.pasan.invitation.service;

import com.pasan.invitation.domain.po.ChatMessage;

import java.util.List;

public interface IChatMessageService {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getHistory(Long inviteId, int limit);
}
