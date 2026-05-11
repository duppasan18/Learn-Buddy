package com.pasan.invitation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pasan.invitation.domain.po.ChatMessage;
import com.pasan.invitation.mapper.ChatMessageMapper;
import com.pasan.invitation.service.IChatMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        message.setCreateTime(LocalDateTime.now());
        save(message);
        return message;
    }

    @Override
    public List<ChatMessage> getHistory(Long inviteId, int limit) {
        Page<ChatMessage> page = new Page<>(1, limit);
        return baseMapper.selectPage(page, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getInviteId, inviteId)
                .orderByDesc(ChatMessage::getCreateTime)).getRecords();
    }
}
