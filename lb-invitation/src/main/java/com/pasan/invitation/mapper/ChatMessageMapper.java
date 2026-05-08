package com.pasan.invitation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pasan.invitation.domain.po.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
