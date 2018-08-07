package com.glodon.zuul.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by liuqc-b on 2018/7/18.
 */
@Controller
public class LoginController {

    @RequestMapping("/login")
    public ModelAndView login(){
        ModelAndView modelAndView=new ModelAndView("index");
        return modelAndView;
    }
    @RequestMapping("test")
    public String test(){
        return "index";
    }
}
