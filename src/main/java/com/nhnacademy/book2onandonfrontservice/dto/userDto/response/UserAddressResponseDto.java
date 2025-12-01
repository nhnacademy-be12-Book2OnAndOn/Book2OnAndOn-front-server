package com.nhnacademy.book2onandonfrontservice.dto.userDto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressResponseDto {
    private Long addressId;
    private String userAddressName;
    private String recipient;
    private String phone;
    private String zipCode;
    private String userAddress;
    private String userAddressDetail;
    private boolean isDefault;
}
