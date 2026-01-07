//package com.nhnacademy.book2onandonfrontservice.config;
//
//import feign.Client;
//import feign.okhttp.OkHttpClient;
//import okhttp3.ConnectionPool;
//import okhttp3.OkHttpClient.Builder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//public class FeignOkHttpConfig {
//
//    @Bean
//    public okhttp3.OkHttpClient okHttp3Client() {
//        return new Builder()
//                .connectTimeout(5, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
//                .retryOnConnectionFailure(true)
//                .build();
//    }
//
//    @Bean
//    public Client feignClient(okhttp3.OkHttpClient client) {
//        return new OkHttpClient(client);
//    }
//}
