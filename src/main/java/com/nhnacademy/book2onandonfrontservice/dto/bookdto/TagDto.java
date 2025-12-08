
package com.nhnacademy.book2onandonfrontservice.dto.bookdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDto {
    private Long id;    // tag_id
    private String name;    // tag_name
}