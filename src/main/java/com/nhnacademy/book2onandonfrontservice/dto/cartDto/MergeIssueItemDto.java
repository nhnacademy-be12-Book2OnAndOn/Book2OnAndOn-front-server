package com.nhnacademy.book2onandonfrontservice.dto.cartDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// merge 시 문제된 항목 정보
public class MergeIssueItemDto {

    private Long bookId;

    private int guestQuantity;
    private int userQuantity;
    private int mergedQuantity;

    private MergeIssueReason reason;
}
