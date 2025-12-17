package com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryAdminAdjustRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private Integer amount; // 지급/차감 절대값

    private String memo; // 보상 사유

    // 클라이언트에서 전달하는 조정 타입 (EARN/USE)
    private String changeType;

    // 차감 타입 (예: USE, CANCEL 등), 차감일 때만 사용
    private String useType;

}
