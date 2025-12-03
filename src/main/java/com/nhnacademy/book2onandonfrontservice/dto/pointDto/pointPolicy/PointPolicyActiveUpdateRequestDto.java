package com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointPolicy;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointPolicyActiveUpdateRequestDto {

    @NotNull
    private Boolean isActive;

}
