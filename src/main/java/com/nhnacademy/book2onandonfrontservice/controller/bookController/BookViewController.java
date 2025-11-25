package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BookViewController {

    private final BookClient bookClient;

    // 메인 페이지 (대시보드)
    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "0") int page, Model model) {
        // 1. 사이드바 카테고리
        List<Map<String, Object>> categories = bookClient.getCategories();
        model.addAttribute("categories", categories);

        // 2. 헤더/히어로 섹션 데이터
        List<String> popularTags = bookClient.getPopularTags();
        model.addAttribute("popularTags", popularTags);

        // 3. 프로모션 박스
        Map<String, Object> promotion = bookClient.getMainPromotion();
        model.addAttribute("promotion", promotion);

        // 4. 메인 도서 그리드 (기본 목록)
        // Gateway -> Book Service로 요청
        Page<BookDto> bookPage = bookClient.getBooks(page, 12); // 한 페이지에 12개

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalBooks", bookPage.getTotalElements());
        model.addAttribute("sectionTitle", "IT · 프로그래밍 도서"); // 기본 타이틀

        return "dashboard";
    }

    // 베스트셀러 페이지 (dashboard.html 재사용)
    @GetMapping("/books/bestseller")
    public String bestseller(Model model) {
        commonData(model); // 카테고리 등 공통 데이터 로드

        List<BookDto> books = bookClient.getBestsellers();
        model.addAttribute("books", books);
        model.addAttribute("sectionTitle", "베스트셀러");
        model.addAttribute("totalBooks", books.size());
        // 베스트셀러는 페이징 없이 전체 노출한다고 가정
        model.addAttribute("totalPages", 1);
        model.addAttribute("currentPage", 0);

        return "dashboard";
    }

    // 신간 페이지
    @GetMapping("/books/new")
    public String newBooks(Model model) {
        commonData(model);

        List<BookDto> books = bookClient.getNewBooks();
        model.addAttribute("books", books);
        model.addAttribute("sectionTitle", "신간 도서");
        model.addAttribute("totalBooks", books.size());

        return "dashboard";
    }

    // 검색 처리
    @GetMapping("/books/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        commonData(model);

        Page<BookDto> result = bookClient.searchBooks(keyword, page, 12);
        model.addAttribute("books", result.getContent());
        model.addAttribute("sectionTitle", "'" + keyword + "' 검색 결과");
        model.addAttribute("keyword", keyword); // 검색어 유지
        model.addAttribute("totalPages", result.getTotalPages());

        return "dashboard";
    }

    // 공통 데이터 (카테고리, 태그 등) 로딩 헬퍼 메서드
    private void commonData(Model model) {
        model.addAttribute("categories", bookClient.getCategories());
        model.addAttribute("popularTags", bookClient.getPopularTags());
    }
}