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
public class EarnOrderPointRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private Long orderItemId;

    @NotNull
    private Integer orderAmount; // 결제 금액 * 적립률

}
