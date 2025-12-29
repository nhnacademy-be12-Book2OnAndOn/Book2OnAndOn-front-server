package com.nhnacademy.book2onandonfrontservice.dto.orderDto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Feign 페이지 응답 매핑용 DTO (PageJacksonModule 의 SimplePageImpl 접근 이슈 회피).
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderPageResponse {
    private List<OrderSimpleDto> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
