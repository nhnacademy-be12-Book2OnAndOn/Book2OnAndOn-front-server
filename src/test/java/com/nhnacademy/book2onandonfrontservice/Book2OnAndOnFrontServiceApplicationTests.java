package com.nhnacademy.book2onandonfrontservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "payco.client-id=dummy",
        "payco.client-secret=ddddd",
        "payco.authorization-uri=daffddd",
        "payco.redirect-uri=fdafd",
})
class Book2OnAndOnFrontServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
