package com.nhnacademy.book2onandonfrontservice.controller.cartController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cartpage")
// 화면(View) 반환용.
public class CartController {

    @GetMapping
    public String cartPage() {
        return "cart";
    }
}
