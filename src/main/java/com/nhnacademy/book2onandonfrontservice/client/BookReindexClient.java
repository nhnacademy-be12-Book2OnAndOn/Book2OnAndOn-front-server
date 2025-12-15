package com.nhnacademy.book2onandonfrontservice.client;

import com.nhnacademy.book2onandonfrontservice.config.FeignMultipartConfig;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.NameUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.TagDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gateway-service", contextId = "bookClient", url = "${gateway.base-url}", configuration = FeignMultipartConfig.class)
public interface BookReindexClient {

    /// 전체 재인덱싱
    @PostMapping("/api/admin/search/reindex")
    String reindexAll();

    /// 특정 카테고리 강제 재인덱싱 (카테고리 하나를 지정해서 강제로 재인덱싱 시킴 / 뭔가 데이터 불일치가 있을때)
    @PostMapping("/api/admin/search/reindex/category/{categoryId}")
    String manualReindexCategory(@PathVariable Long categoryId);

    /// 특정 태그 강제 재인덱싱 (태그 하나를 지정해서 강제로 재인덱싱 시킴 / 뭔가 데이터 불일치가 있을때)
    @PostMapping("/api/admin/search/reindex/tag/{tagId}")
    String manualReindexTag(@PathVariable Long tagId);

    ///  ------------ 카테고리 / 태그 이름 수정 ---------------

    /// 카테고리 이름 수정 (카테고리 이름을 수정하면 백엔드에서 그와 관련된 모든 책을 reindex처리함)
    @PutMapping("/api/admin/categories/{categoryId}")
    CategoryDto updateCategoryName(@PathVariable Long categoryId, @RequestBody NameUpdateRequest request);

    /// 태그 이름 수정 (태그 이름을 수정하면 백엔드에서 그와 관련된 모든 책을 reindex처리함)
    @PutMapping("/api/admin/tags/{tagId}")
    TagDto updateTagName(@PathVariable Long tagId, @RequestBody NameUpdateRequest request);
}
