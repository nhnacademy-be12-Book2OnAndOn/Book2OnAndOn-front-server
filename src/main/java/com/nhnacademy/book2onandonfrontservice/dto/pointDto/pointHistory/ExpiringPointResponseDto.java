package com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpiringPointResponseDto {
    private int expiringAmount;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}