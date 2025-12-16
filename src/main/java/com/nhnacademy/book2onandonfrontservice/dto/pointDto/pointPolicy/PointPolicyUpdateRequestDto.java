package com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PointPolicyUpdateRequestDto {

    // 고정 포인트 적립 (null 허용)
    @PositiveOrZero(message = "포인트는 0 이상이어야 합니다.")
    private Integer pointAddPoint;

}
