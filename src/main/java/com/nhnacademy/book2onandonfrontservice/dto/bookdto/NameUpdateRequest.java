package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NameUpdateRequest {
    private String newName;
}