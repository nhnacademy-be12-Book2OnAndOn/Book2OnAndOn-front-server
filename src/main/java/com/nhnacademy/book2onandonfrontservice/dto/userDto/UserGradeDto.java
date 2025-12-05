package com.nhnacademy.book2onandonfrontservice.dto.userDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserGradeDto {
    private Long gradeId;
    private String gradeName;
    private Double pointAddRate;
    private Integer pointCutline;
}
