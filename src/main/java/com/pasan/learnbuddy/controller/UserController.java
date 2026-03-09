package com.pasan.learnbuddy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pasan.learnbuddy.constants.MessageConstant;
import com.pasan.learnbuddy.domain.dto.UserLoginDTO;
import com.pasan.learnbuddy.domain.po.Result;
import com.pasan.learnbuddy.domain.po.User;
import com.pasan.learnbuddy.domain.vo.UserLoginVO;
import com.pasan.learnbuddy.exception.BusinessException;
import com.pasan.learnbuddy.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 微信登录
     */
    @PostMapping("/login")
    public Result<UserLoginVO> wxLogin(@RequestBody UserLoginDTO userLoginDTO) {
        return Result.success(userService.wxLogin(userLoginDTO));
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(userService.getUserInfo(userId));
    }

}
