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
    private Long orderId;

    @NotNull
    private Integer pureAmount;

    @NotNull
    private Double pointAddRate; // <- 회원 등급 적립률 받아옴

}
