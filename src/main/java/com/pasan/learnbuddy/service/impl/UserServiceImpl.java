package com.pasan.learnbuddy.service.impl;

import com.pasan.learnbuddy.domain.po.User;
import com.pasan.learnbuddy.mapper.UserMapper;
import com.pasan.learnbuddy.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author pasan
 * @since 2026-03-04
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
