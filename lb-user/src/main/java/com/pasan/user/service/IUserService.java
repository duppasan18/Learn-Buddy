package com.pasan.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pasan.user.domain.dto.TestLoginDTO;
import com.pasan.user.domain.dto.UserLoginDTO;
import com.pasan.user.domain.po.User;
import com.pasan.user.domain.vo.UserInfoVO;
import com.pasan.user.domain.vo.UserLoginVO;

import java.util.List;
import java.util.Map;

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

    List<UserInfoVO> getUserInfos(List<Long> ids);

    UserLoginVO testLogin(TestLoginDTO dto);

    void logout();

    void updateUserInfo(Map<String, String> body);
}
