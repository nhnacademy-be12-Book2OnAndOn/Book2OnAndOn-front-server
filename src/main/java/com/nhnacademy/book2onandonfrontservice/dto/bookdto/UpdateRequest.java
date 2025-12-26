package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class UpdateRequest {
    private String newName;
}