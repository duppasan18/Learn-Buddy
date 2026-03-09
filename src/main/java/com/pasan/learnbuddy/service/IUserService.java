package com.pasan.learnbuddy.service;

import com.pasan.learnbuddy.domain.dto.UserLoginDTO;
import com.pasan.learnbuddy.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pasan.learnbuddy.domain.vo.UserLoginVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author pasan
 * @since 2026-03-04
 */
public interface IUserService extends IService<User> {

    UserLoginVO wxLogin(UserLoginDTO userLoginDTO);

    User getUserInfo(Long userId);
}
