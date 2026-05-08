package com.pasan.invitation.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pasan.constants.RedisConstant;
import com.pasan.exception.BusinessException;
import com.pasan.invitation.domain.dto.StudyStartDTO;
import com.pasan.invitation.domain.po.StudyRecord;
import com.pasan.invitation.domain.vo.StudyStatusVO;
import com.pasan.invitation.mapper.StudyRecordMapper;
import com.pasan.invitation.service.IStudyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudyRecordServiceImpl extends ServiceImpl<StudyRecordMapper, StudyRecord> implements IStudyRecordService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void startStudy(StudyStartDTO dto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String activeKey = RedisConstant.STUDY_ACTIVE_KEY_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(activeKey))) {
            throw new BusinessException("当前已有进行中的学习");
        }

        JSONObject cache = new JSONObject();
        cache.put("startTime", LocalDateTime.now().toString());
        cache.put("subject", dto.getSubject());
        cache.put("roomId", dto.getRoomId());
        cache.put("inviteId", dto.getInviteId());
        redisTemplate.opsForValue().set(activeKey, cache.toJSONString(), Duration.ofHours(24));
    }

    @Override
    public void stopStudy() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String activeKey = RedisConstant.STUDY_ACTIVE_KEY_PREFIX + userId;
        String cacheStr = redisTemplate.opsForValue().get(activeKey);
        if (cacheStr == null) {
            throw new BusinessException("当前没有进行中的学习");
        }
        JSONObject cache = JSON.parseObject(cacheStr);
        LocalDateTime startTime = LocalDateTime.parse(cache.getString("startTime"));
        String subject = cache.getString("subject");
        Long roomId = cache.getLong("roomId");
        Long inviteId = cache.getLong("inviteId");

        LocalDateTime now = LocalDateTime.now();
        int durationMin = (int) Duration.between(startTime, now).toMinutes();

        if (durationMin > 0) {
            StudyRecord record = new StudyRecord();
            record.setUserId(userId);
            record.setSubject(subject);
            record.setRoomId(roomId);
            record.setInviteId(inviteId);
            record.setStartTime(startTime);
            record.setEndTime(now);
            record.setDurationMin(durationMin);
            record.setStatus(1);
            save(record);
            redisTemplate.opsForZSet().incrementScore(RedisConstant.STUDY_RANKING_KEY, userId.toString(), durationMin);
        }

        redisTemplate.delete(activeKey);
    }

    @Override
    public StudyStatusVO getStatus() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String activeKey = RedisConstant.STUDY_ACTIVE_KEY_PREFIX + userId;
        String cacheStr = redisTemplate.opsForValue().get(activeKey);
        if (cacheStr == null) {
            return StudyStatusVO.builder().studying(false).build();
        }
        JSONObject cache = JSON.parseObject(cacheStr);
        LocalDateTime startTime = LocalDateTime.parse(cache.getString("startTime"));
        long durationSec = Duration.between(startTime, LocalDateTime.now()).getSeconds();

        return StudyStatusVO.builder()
                .studying(true)
                .subject(cache.getString("subject"))
                .startTime(startTime)
                .durationSec(durationSec)
                .build();
    }

    @Override
    public int getTodayDuration() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return baseMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId)
                        .eq(StudyRecord::getStatus, 1)
                        .ge(StudyRecord::getEndTime, todayStart))
                .stream()
                .mapToInt(r -> r.getDurationMin() == null ? 0 : r.getDurationMin())
                .sum();
    }

    @Override
    public Map<String, Double> getRanking(int topN) {
        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RedisConstant.STUDY_RANKING_KEY, 0, topN - 1);
        Map<String, Double> ranking = new LinkedHashMap<>();
        if (set != null) {
            for (ZSetOperations.TypedTuple<String> tuple : set) {
                ranking.put(tuple.getValue(), tuple.getScore());
            }
        }
        return ranking;
    }
}
