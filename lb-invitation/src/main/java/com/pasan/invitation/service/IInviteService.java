package com.pasan.invitation.service;

import com.pasan.invitation.domain.dto.AttendInviteDTO;
import com.pasan.invitation.domain.dto.InviteDTO;
import com.pasan.invitation.domain.po.Invite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pasan.invitation.domain.vo.InviteVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author pasan
 * @since 2026-04-14
 */
public interface IInviteService extends IService<Invite> {

    void createInvite(InviteDTO dto);

    List<InviteVO> getNearbyInvite();

    void joinInvite(AttendInviteDTO dto);

    InviteVO getInviteDetail(Long inviteId);

    void extendInvite(Long inviteId);

    void leaveInvite(Long inviteId);

    void cancelInvite(Long inviteId);

    InviteVO getMyActiveInvite();
}
