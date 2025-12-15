package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.dto.userDto.UserGradeDto;
import com.nhnacademy.book2onandonfrontservice.dto.userDto.request.UserGradeRequestDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gateway-service", contextId = "userGradeClient", url = "${gateway.base-url}")
public interface UserGradeClient {
    // 전체 등급 조회
    @GetMapping("/api/grades")
    List<UserGradeDto> getAllGrades();

    // 등급 생성
    @PostMapping("/api/admin/grades")
    void createGrade(@RequestHeader("Authorization") String accessToken, @RequestBody UserGradeRequestDto request);

    // 등급 수정
    @PutMapping("/api/admin/grades/{gradeId}")
    void updateGrade(@RequestHeader("Authorization") String accessToken, @PathVariable("gradeId") Long gradeId,
                     @RequestBody UserGradeRequestDto request);


}
