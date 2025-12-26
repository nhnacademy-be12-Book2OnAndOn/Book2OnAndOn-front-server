package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

public record UserAddressResponseDto(Long addressId,
                                     String userAddressName,
                                     String recipient,
                                     String phone,
                                     String zipCode,
                                     String userAddress,
                                     String userAddressDetail,
                                     boolean isDefault) {
}
