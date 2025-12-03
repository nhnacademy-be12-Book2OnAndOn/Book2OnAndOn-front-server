package com.nhnacademy.book2onandonfrontservice.dto.pointDto.pointHistory;

import com.nhnacademy.book2onandonfrontservice.dto.pointDto.PointReason;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointHistoryResponseDto {

    private Long pointHistoryId;
    private int pointHistoryChange;
    private int totalPoints;
    private LocalDateTime pointCreatedDate;
    private LocalDateTime pointExpiredDate;
    private Integer remainingPoint;
    private PointReason pointReason;
    private Long orderItemId;
    private Long reviewId;
    private Long returnId;

}
