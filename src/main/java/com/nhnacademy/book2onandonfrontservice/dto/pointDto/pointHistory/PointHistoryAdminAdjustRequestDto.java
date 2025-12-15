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
    private Integer amount;

    private String memo; // 보상 사유

}
