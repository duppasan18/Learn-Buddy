package com.pasan.user.controller;

import com.pasan.result.Result;
import com.pasan.user.domain.dto.TestLoginDTO;
import com.pasan.user.domain.dto.UserLoginDTO;
import com.pasan.user.domain.vo.UserInfoVO;
import com.pasan.user.domain.vo.UserLoginVO;
import com.pasan.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * 批量获取用户信息
     */
    @GetMapping("/infos")
    public List<UserInfoVO> getUserInfos(@RequestParam("ids") List<Long> ids){
        return userService.getUserInfos(ids);
    }

    /**
     * ApiFox测试登录（不走微信API）
     */
    @PostMapping("/loginTest")
    public Result<UserLoginVO> loginTest(@RequestBody TestLoginDTO dto) {
        return Result.success(userService.testLogin(dto));
    }

    /**
     * 退出登录（清除token和位置缓存）
     */
    @PostMapping("/logout")
    public Result logout() {
        userService.logout();
        return Result.success();
    }

    /** 修改用户信息（昵称、头像） */
    @PutMapping("/info")
    public Result updateUserInfo(@RequestBody Map<String, String> body) {
        userService.updateUserInfo(body);
        return Result.success();
    }

}
