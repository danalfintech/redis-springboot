package com.example.springbootredistest.controller;

import com.example.springbootredistest.service.RedisService;
import com.example.springbootredistest.vo.RedisInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {
    @Autowired
    private RedisService redisService;

    // 캐시 등록
    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json; charset=utf8")
    public Object register(@RequestBody RedisInfo redisInfo) {
        redisService.addKey(redisInfo.getKey(), redisInfo.getValue());
        return redisInfo;
    }

    // 캐시에 저장된 key의 값 가져오기
    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=utf8")
    public Object get(@RequestBody RedisInfo redisInfo) {
        String value = redisService.getValue(redisInfo.getKey());
        redisInfo.setValue(value);
        return redisInfo;
    }


}
