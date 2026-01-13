package com.nhnacademy.book2onandonfrontservice.dto.deliveryDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryWaybillUpdateDto {
    @NotNull(message = "택배사를 입력해주세요.")
    private String deliveryCompany;

    @NotNull(message = "운송장 번호를 입력해주세요.")
    private String waybill;
}
