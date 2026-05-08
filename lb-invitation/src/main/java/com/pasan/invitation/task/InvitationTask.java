package com.pasan.invitation.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pasan.invitation.domain.enums.InviteStatus;
import com.pasan.invitation.domain.po.Invite;
import com.pasan.invitation.mapper.InviteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class InvitationTask {

    @Autowired
    private InviteMapper inviteMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processExpiredInvite(){
        LocalDateTime now = LocalDateTime.now();

        inviteMapper.update(new LambdaUpdateWrapper<Invite>()
                .eq(Invite::getStatus, InviteStatus.PROCESSING)
                .lt(Invite::getEndTime, now)
                .set(Invite::getStatus, InviteStatus.FINISHED));
    }
}
