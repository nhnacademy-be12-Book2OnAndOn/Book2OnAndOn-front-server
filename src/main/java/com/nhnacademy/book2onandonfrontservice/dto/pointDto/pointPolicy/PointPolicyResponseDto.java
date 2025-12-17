package com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointPolicyResponseDto {

    private Integer pointPolicyId;
    private String pointPolicyName;
    private Integer pointAddPoint;
    private Boolean pointIsActive;

}
