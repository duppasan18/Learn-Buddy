package com.pasan.invitation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pasan.client.LocationClient;
import com.pasan.client.UserClient;
import com.pasan.constants.RedisConstant;
import com.pasan.dto.Location;
import com.pasan.exception.BusinessException;
import com.pasan.invitation.domain.dto.AttendInviteDTO;
import com.pasan.invitation.domain.dto.InviteDTO;
import com.pasan.invitation.domain.enums.InviteStatus;
import com.pasan.invitation.domain.po.Invite;
import com.pasan.invitation.domain.po.InviteMember;
import com.pasan.invitation.domain.vo.InviteVO;
import com.pasan.invitation.mapper.InviteMapper;
import com.pasan.invitation.mapper.InviteMemberMapper;
import com.pasan.invitation.service.IInviteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pasan.vo.RoomVO;
import com.pasan.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author pasan
 * @since 2026-04-14
 */
@Service
@RequiredArgsConstructor
public class InviteServiceImpl extends ServiceImpl<InviteMapper, Invite> implements IInviteService {

    private final InviteMemberMapper inviteMemberMapper;
    private final LocationClient locationClient;
    private final UserClient userClient;

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInvite(InviteDTO dto) {
        // 获取用户id
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 查询是否有正在进行的邀请
        Invite processingInvite = lambdaQuery().eq(Invite::getUserId, userId)
                .eq(Invite::getStatus, InviteStatus.PROCESSING).one();
        if(processingInvite != null){
            throw new BusinessException("您有正在进行的自习");
        }
        // 封装数据
        Invite invite = BeanUtil.copyProperties(dto, Invite.class);
        invite.setUserId(userId);
        save(invite);
        // 添加成员
        InviteMember member = new InviteMember();
        member.setInviteId(invite.getId());
        member.setUserId(userId);
        inviteMemberMapper.insert(member);
        // 获取对应的位置信息
        Long roomId = dto.getRoomId();
        Location roomLocation = locationClient.getRoomLocation(roomId);
        Point point = new Point(roomLocation.getLongitude(), roomLocation.getLatitude());
        // 更新缓存数据(邀约人数、邀约位置)
        String key = RedisConstant.INVITATION_COUNT_KEY_PREFIX + invite.getId();
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.opsForGeo().add(RedisConstant.INVITATION_LOCATION_KEY, point, invite.getId().toString());
    }

    @Override
    public List<InviteVO> getNearbyInvite() {
        // 查找附近邀约id
        List<Long> ids = locationClient.nearbyRoomInvite();
        if(CollUtil.isEmpty(ids)){
            return List.of();
        }
        // 查找邀约详细信息
        List<Invite> list = lambdaQuery()
                .in(Invite::getId, ids)
                .eq(Invite::getStatus, InviteStatus.PROCESSING)
                .list();
        if(CollUtil.isEmpty(list)){
            throw new BusinessException("无法查询邀约信息");
        }
        Map<Long, Invite> inviteMap = list.stream().collect(Collectors.toMap(Invite::getId, invite -> invite, (a, b) -> a));
        // 获取用户id和房间id列表
        List<Long> userIds = list.stream().map(Invite::getUserId).collect(Collectors.toList());
        List<Long> roomIds = list.stream().map(Invite::getRoomId).collect(Collectors.toList());
        // 去重后查询，避免同一用户/房间多次查询和toMap重复key
        List<Long> distinctUserIds = userIds.stream().distinct().toList();
        List<Long> distinctRoomIds = roomIds.stream().distinct().toList();
        List<UserInfoVO> userInfos = userClient.getUserInfos(distinctUserIds);
        Map<Long, UserInfoVO> userMap = userInfos.stream()
                .collect(Collectors.toMap(UserInfoVO::getId, vo -> vo, (a, b) -> a));
        List<RoomVO> roomInfos = locationClient.getRoomInfos(distinctRoomIds);
        Map<Long, RoomVO> roomMap = roomInfos.stream()
                .collect(Collectors.toMap(RoomVO::getId, vo -> vo, (a, b) -> a));
        // 组装数据
        List<InviteVO> inviteVOS = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Invite invite = inviteMap.get(id);
            if(invite == null){
                continue;
            }
            InviteVO vo = BeanUtil.copyProperties(invite, InviteVO.class);
            UserInfoVO userInfo = userMap.get(invite.getUserId());
            if(userInfo != null){
                vo.setUserName(userInfo.getName());
            }
            RoomVO roomInfo = roomMap.get(invite.getRoomId());
            if(roomInfo != null){
                vo.setRoomName(roomInfo.getName());
            }
            inviteVOS.add(vo);
            // 获取已参与人数
            String numStr = redisTemplate.opsForValue().get(RedisConstant.INVITATION_COUNT_KEY_PREFIX + id);
            vo.setJoinMembers(numStr == null ? 0 : Integer.parseInt(numStr));
        }
        return inviteVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinInvite(AttendInviteDTO dto) {
        // 获取用户id
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 获取邀约id
        Long inviteId = dto.getInviteId();

        // Redisson分布式锁，防止用户并发加入多个邀约
        String lockKey = RedisConstant.INVITATION_LOCK_KEY_PREFIX + "user:" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后再试");
            }
            // 查看是否已参与其他邀约
            InviteMember otherMember = inviteMemberMapper.selectOne(new LambdaQueryWrapper<InviteMember>()
                    .eq(InviteMember::getStatus, 0)
                    .eq(InviteMember::getUserId, userId));
            if (otherMember != null && !otherMember.getInviteId().equals(inviteId)) {
                throw new BusinessException("您已参加了一个邀约");
            }
            Invite invite = lambdaQuery().eq(Invite::getId, inviteId).one();
            if (invite == null) {
                throw new BusinessException("无法查询邀约信息");
            }
            // 查看是否已满人（重加入不增加人数）
            boolean rejoin = otherMember != null;
            if (!rejoin) {
                String countStr = redisTemplate.opsForValue().get(RedisConstant.INVITATION_COUNT_KEY_PREFIX + inviteId);
                int currentCount = countStr == null ? 0 : Integer.parseInt(countStr);
                if (currentCount >= invite.getMaxMembers()) {
                    throw new BusinessException("该小组已满人");
                }
            }
            // 插入或更新成员记录
            InviteMember existMember = inviteMemberMapper.selectOne(new LambdaQueryWrapper<InviteMember>()
                    .eq(InviteMember::getInviteId, inviteId)
                    .eq(InviteMember::getUserId, userId));
            if (existMember != null) {
                existMember.setStatus(0);
                inviteMemberMapper.updateById(existMember);
            } else {
                InviteMember attendMember = new InviteMember();
                attendMember.setInviteId(inviteId);
                attendMember.setUserId(userId);
                inviteMemberMapper.insert(attendMember);
            }
            if (!rejoin) {
                redisTemplate.opsForValue().increment(RedisConstant.INVITATION_COUNT_KEY_PREFIX + inviteId, 1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }

    }

    @Override
    public InviteVO getInviteDetail(Long inviteId) {
        Invite invite = getById(inviteId);
        if (invite == null) {
            throw new BusinessException("邀约不存在");
        }
        InviteVO vo = BeanUtil.copyProperties(invite, InviteVO.class);

        UserInfoVO userInfo = userClient.getUserInfos(List.of(invite.getUserId()))
                .stream().findFirst().orElse(null);
        if (userInfo != null) {
            vo.setUserName(userInfo.getName());
        }

        RoomVO roomInfo = locationClient.getRoomInfos(List.of(invite.getRoomId()))
                .stream().findFirst().orElse(null);
        if (roomInfo != null) {
            vo.setRoomName(roomInfo.getName());
        }

        String numStr = redisTemplate.opsForValue().get(RedisConstant.INVITATION_COUNT_KEY_PREFIX + inviteId);
        vo.setJoinMembers(numStr == null ? 0 : Integer.parseInt(numStr));
        return vo;
    }

    @Override
    public void extendInvite(Long inviteId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Invite invite = getById(inviteId);
        if (invite == null) {
            throw new BusinessException("邀约不存在");
        }
        if (!invite.getUserId().equals(userId)) {
            throw new BusinessException("仅发布者可延长邀约");
        }
        invite.setEndTime(invite.getEndTime().plusMinutes(30));
        updateById(invite);
    }

    @Override
    public void leaveInvite(Long inviteId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        InviteMember member = inviteMemberMapper.selectOne(new LambdaQueryWrapper<InviteMember>()
                .eq(InviteMember::getInviteId, inviteId)
                .eq(InviteMember::getUserId, userId)
                .eq(InviteMember::getStatus, 0));
        if (member == null) {
            throw new BusinessException("未参与该邀约");
        }
        member.setStatus(1);
        inviteMemberMapper.updateById(member);
        redisTemplate.opsForValue().decrement(RedisConstant.INVITATION_COUNT_KEY_PREFIX + inviteId);
    }

    @Override
    public void cancelInvite(Long inviteId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Invite invite = getById(inviteId);
        if (invite == null) {
            throw new BusinessException("邀约不存在");
        }
        if (!invite.getUserId().equals(userId)) {
            throw new BusinessException("仅发布者可解散邀约");
        }
        invite.setStatus(InviteStatus.CANCELED);
        updateById(invite);
    }

    @Override
    public InviteVO getMyActiveInvite() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        InviteMember member = inviteMemberMapper.selectOne(new LambdaQueryWrapper<InviteMember>()
                .eq(InviteMember::getUserId, userId)
                .eq(InviteMember::getStatus, 0));
        if (member == null) return null;
        return getInviteDetail(member.getInviteId());
    }
}
