package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookStatusUpdateRequest {

    @NotNull(message = "변경할 상태값은 필수입니다.")
    private BookStatus status;
}