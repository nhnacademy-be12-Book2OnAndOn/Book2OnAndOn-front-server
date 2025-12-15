package com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory;

import com.nhnacademy.book2onandonfrontservice.dto.pointDto.PointReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EarnPointResponseDto {

    private int changedPoint;
    private int totalPointAfter;
    private PointReason earnReason;    // SIGNUP / REVIEW / ORDER / REFUND / ADMIN_ADJUST 등

}

