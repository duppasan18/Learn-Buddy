package com.pasan.client;



import com.pasan.vo.UserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("lb-user")
public interface UserClient {

    @GetMapping("/user/infos")
    List<UserInfoVO> getUserInfos(@RequestParam("ids") Iterable<Long> ids);

}
