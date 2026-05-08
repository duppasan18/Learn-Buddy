package com.pasan.user.controller;

import com.pasan.result.Result;
import com.pasan.user.domain.dto.TestLoginDTO;
import com.pasan.user.domain.dto.UserLoginDTO;
import com.pasan.user.domain.vo.UserInfoVO;
import com.pasan.user.domain.vo.UserLoginVO;
import com.pasan.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 批量获取用户信息
     */
    @GetMapping("/infos")
    public List<UserInfoVO> getUserInfos(@RequestParam("ids") List<Long> ids){
        return userService.getUserInfos(ids);
    }

    /**
     * 为用户签发JWT令牌（内部调用）
     */
    @GetMapping("/token/{id}")
    public String getToken(@PathVariable Long id){
        return userService.getToken(id);
    }

    /**
     * ApiFox测试登录（不走微信API）
     */
    @PostMapping("/loginTest")
    public Result<UserLoginVO> loginTest(@RequestBody TestLoginDTO dto) {
        return Result.success(userService.testLogin(dto));
    }

}
