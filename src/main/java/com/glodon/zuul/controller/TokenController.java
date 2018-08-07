package com.glodon.zuul.controller;

import com.alibaba.fastjson.JSON;
import com.glodon.zuul.model.UserInfo;
import com.glodon.zuul.service.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by liuqc-b on 2018/7/9.
 */
@RestController
public class TokenController {

    @Autowired
    AuthService authService;

    @PostMapping("/auth/tokens")
    public String authLogin(@RequestBody String data){
        UserInfo userInfo= JSON.parseObject(data,UserInfo.class);
        String userName=userInfo.getUserName();
        String password=userInfo.getPassword();
        Map<String,String> map= authService.createToken(userName,password);
        return map.toString();
    }

    @GetMapping("/auth/verify")
    public String verify(HttpServletRequest httpServletRequest) throws Exception {
        String token=httpServletRequest.getHeader("token");
        if(token==null||token==""){
            return "token is null";
        }
        Claims claims=authService.getClaimsFromToken(token);
        String userName= (String) claims.get("userName");
        return "当前登录用户信息为"+userName;
    }
    @GetMapping("/hello")
    public String info(){
        return "ok";
    }



}
