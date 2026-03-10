package com.pasan.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pasan.user.domain.dto.UserLoginDTO;
import com.pasan.user.domain.po.User;
import com.pasan.user.domain.vo.UserLoginVO;

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
