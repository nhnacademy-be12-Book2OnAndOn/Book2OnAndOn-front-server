package com.nhnacademy.book2onandonfrontservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class home {

    @RequestMapping("/")
    public String home(){
        return "home";
    }
}
