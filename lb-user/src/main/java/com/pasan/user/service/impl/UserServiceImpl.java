package com.pasan.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pasan.config.wechat.WechatProperties;
import com.pasan.constants.JwtClaimsConstant;
import com.pasan.constants.MessageConstant;
import com.pasan.constants.OssConstant;
import com.pasan.constants.RedisConstant;
import com.pasan.exception.BusinessException;
import com.pasan.exception.LoginFailedException;
import com.pasan.user.domain.dto.TestLoginDTO;
import com.pasan.user.domain.dto.UserLoginDTO;
import com.pasan.user.domain.enums.Gender;
import com.pasan.user.domain.po.User;
import com.pasan.user.domain.vo.UserInfoVO;
import com.pasan.user.domain.vo.UserLoginVO;
import com.pasan.user.mapper.UserMapper;
import com.pasan.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pasan.util.HttpClientUtil;
import com.pasan.util.JwtUtil;
import com.pasan.util.RandomStringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author pasan
 * @since 2026-03-04
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final WechatProperties wechatProperties;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());
        //判断openid是否为空
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //根据openid查询用户信息
        User user = lambdaQuery().eq(User::getOpenid, openid)
                .one();
        //新用户则自动完成注册
        if(user == null){
            //公共字段的aop只能填充带有createTime和createUser属性的对象
            //User不具有createUser属性
            user = User.builder()
                    .name("学伴"+ RandomStringUtil.generate(5))
                    .avatar(OssConstant.DEFAULT_AVATAR)
                    .gender(Gender.SECRET)
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            save(user);
        }
        //生成jwt令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = jwtUtil.createJWT(claims);

        redisTemplate.opsForValue().set(RedisConstant.USER_TOKEN_KEY_PREFIX + user.getId(), token, jwtUtil.getExpireSeconds(), TimeUnit.SECONDS);

        return UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .name(user.getName())
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * 获取指定用户信息
     * @return
     */
    @Override
    public List<UserInfoVO> getUserInfos(List<Long> ids) {
        // 获取用户信息
        List<User> list = lambdaQuery()
                .in(User::getId, ids)
                .list();
        // 判空
        if(CollUtil.isEmpty(list)){
            return List.of();
        }
        // 不为空则复制属性返回
        List<UserInfoVO> vos = BeanUtil.copyToList(list, UserInfoVO.class);
        return vos;
    }

    @Override
    public UserLoginVO testLogin(TestLoginDTO dto) {
        Long userId = dto.getUserId();
        User user;
        if (userId != null) {
            user = getById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在: " + userId);
            }
        } else {
            String name = dto.getName() != null ? dto.getName() : "测试" + RandomStringUtil.generate(5);
            user = User.builder()
                    .name(name)
                    .openid("test_" + UUID.randomUUID())
                    .avatar(OssConstant.DEFAULT_AVATAR)
                    .gender(Gender.SECRET)
                    .createTime(LocalDateTime.now())
                    .build();
            save(user);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = jwtUtil.createJWT(claims);
        redisTemplate.opsForValue().set(RedisConstant.USER_TOKEN_KEY_PREFIX + user.getId(), token, jwtUtil.getExpireSeconds(), TimeUnit.SECONDS);

        return UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .name(user.getName())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public void logout() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        redisTemplate.delete(RedisConstant.USER_TOKEN_KEY_PREFIX + userId);
        redisTemplate.opsForGeo().remove(RedisConstant.USER_LOCATION_KEY, userId.toString());
    }

    @Override
    public void updateUserInfo(Map<String, String> body) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        String name = body.get("name");
        String avatar = body.get("avatar");
        if (name != null && !name.isEmpty()) user.setName(name);
        if (avatar != null && !avatar.isEmpty()) user.setAvatar(avatar);
        updateById(user);
    }

    /**
     * 调用微信接口获取微信用户openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        // 调用微信接口，获取用户openid
        Map map = new HashMap<>();
        map.put("appid", wechatProperties.getAppId());
        map.put("secret", wechatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String jsonString = HttpClientUtil.doGet(WX_LOGIN_URL, map);
        //截取openid
        JSONObject jsonObject = JSON.parseObject(jsonString);
        String openid = jsonObject.getString("openid");

        return openid;
    }

}
