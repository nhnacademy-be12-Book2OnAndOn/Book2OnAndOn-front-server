package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberCouponTargetResponseDto extends MemberCouponResponseDto{

    private List<Long> targetBookIds = new ArrayList<>();
    private List<Long> targetCategoryIds = new ArrayList<>();

    public MemberCouponTargetResponseDto(MemberCouponResponseDto resp){
        super(resp);
    }
}
