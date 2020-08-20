package com.example.throttling.controller;

import com.example.throttling.annotation.EnableIPLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author : Harry
 * Description : 测试
 * Date : 2020-08-20 16:53
 */
@RestController
public class ThrottlingTestController {

    /**
     * 单个ip访问该接口 2 秒内如果超过 20 次, 该ip 120 分钟内无法继续访问该接口
     */
    @EnableIPLimit(limitCount = 20, time = 2, lockTime = 120)
    @GetMapping(value = "/test")
    public String test() {

        return "success";
    }
}
