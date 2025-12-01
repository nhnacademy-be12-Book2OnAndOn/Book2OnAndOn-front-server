package com.nhnacademy.book2onandonfrontservice.dto.userDto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressCreateRequest {
    private String userAddressName;
    private String recipient;
    private String phone;
    private String zipCode;
    private String userAddress;
    private String userAddressDetail;
    private boolean isDefault;

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
