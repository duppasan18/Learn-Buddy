package com.pasan.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pasan.config.wechat.WechatProperties;
import com.pasan.constants.JwtClaimsConstant;
import com.pasan.constants.MessageConstant;
import com.pasan.exception.BusinessException;
import com.pasan.exception.LoginFailedException;
import com.pasan.user.domain.dto.UserLoginDTO;
import com.pasan.user.domain.po.User;
import com.pasan.user.domain.vo.UserLoginVO;
import com.pasan.user.mapper.UserMapper;
import com.pasan.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pasan.util.HttpClientUtil;
import com.pasan.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            save(user);
        }
        //生成jwt令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = jwtUtil.createJWT(claims);

        return UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .build();
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @Override
    public User getUserInfo(Long userId) {
        User user = getById(userId);
        if(user == null){
            throw new BusinessException(MessageConstant.USER_NOT_FOUND);
        }

        return user;
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
