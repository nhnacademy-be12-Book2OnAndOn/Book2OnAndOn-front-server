package com.nhnacademy.book2onandonfrontservice.controller.adminController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.client.BookReindexClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSaveRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookSearchCondition;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookStatus;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookStatusUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookUpdateRequest;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.CategoryDto;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.TagDto;
import com.nhnacademy.book2onandonfrontservice.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BookAdminController {
    private final BookClient bookClient;
    private final BookReindexClient bookReindexClient;

    /// --------------------------Book Admin ------------------------------------

    /// 도서 등록
    @PostMapping("/books/create")
    public String createBook(HttpServletRequest request, @ModelAttribute(value = "book") BookSaveRequest req,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        log.info("북서비스 등록 BookSaveRequest: {}", req.toString());
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        bookClient.createBook(token, req, images);
        return "redirect:/admin/books";
    }

    /// 도서 등록 페이지
    @GetMapping("/books/create")
    public String bookCreateForm(Model model) {
        List<CategoryDto> categories = bookClient.getCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("statuses", BookStatus.values());
        return "admin/books/create";
    }

    /// 도서 목록 & 검색
    @GetMapping("/books")
    public String listBooks(@ModelAttribute BookSearchCondition condition,
                            @PageableDefault(size = 10) Pageable pageable,
                            Model model) {
        Page<BookDto> result = Page.empty(pageable);
        try {
            result = bookClient.searchBooks(null, condition, pageable);
        } catch (Exception e) {
            log.error("관리자 도서 목록 조회 실패", e);
        }
        model.addAttribute("page", result);
        model.addAttribute("books", result.getContent());
        model.addAttribute("condition", condition);
        return "admin/books/list";
    }

    @PostMapping("/books/reindex/all")
    public String reindexAll(RedirectAttributes redirectAttributes) {
        try {
            bookReindexClient.reindexAll();
            redirectAttributes.addFlashAttribute("reindexMessage", "전체 재인덱싱을 요청했습니다.");
        } catch (Exception e) {
            log.error("전체 재인덱싱 요청 실패", e);
            redirectAttributes.addFlashAttribute("reindexError", "전체 재인덱싱 요청에 실패했습니다.");
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/books/reindex/category")
    public String reindexCategory(@RequestParam Long categoryId, RedirectAttributes redirectAttributes) {
        try {
            bookReindexClient.manualReindexCategory(categoryId);
            redirectAttributes.addFlashAttribute("reindexMessage", "카테고리 " + categoryId + " 재인덱싱을 요청했습니다.");
        } catch (Exception e) {
            log.error("카테고리 재인덱싱 요청 실패: {}", categoryId, e);
            redirectAttributes.addFlashAttribute("reindexError", "카테고리 재인덱싱 요청에 실패했습니다.");
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/books/reindex/tag")
    public String reindexTag(@RequestParam Long tagId, RedirectAttributes redirectAttributes) {
        try {
            bookReindexClient.manualReindexTag(tagId);
            redirectAttributes.addFlashAttribute("reindexMessage", "태그 " + tagId + " 재인덱싱을 요청했습니다.");
        } catch (Exception e) {
            log.error("태그 재인덱싱 요청 실패: {}", tagId, e);
            redirectAttributes.addFlashAttribute("reindexError", "태그 재인덱싱 요청에 실패했습니다.");
        }
        return "redirect:/admin/books";
    }

    /// 도서 수정 페이지
    @GetMapping("/books/{bookId}/edit")
    public String editBook(@PathVariable Long bookId, Model model) {
        BookDetailResponse book = bookClient.getBookDetail(bookId);
        List<CategoryDto> categories = bookClient.getCategories();
        Long selectedCategoryId = (book != null && book.getCategories() != null && !book.getCategories().isEmpty())
                ? book.getCategories().get(book.getCategories().size() - 1).getId()
                : null;

        model.addAttribute("book", book);
        model.addAttribute("statuses", BookStatus.values());
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        model.addAttribute("tagString", buildTagString(book));
        return "admin/books/edit";
    }

    /// 도서 정보
    @GetMapping("/books/search")
    @ResponseBody // HTML이 아니라 JSON 데이터를 반환
    public ResponseEntity<BookSaveRequest> searchBookInfo(HttpServletRequest request, @RequestParam String isbn) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        try {
            BookSaveRequest bookInfo = bookClient.lookupBook(token, isbn);
            return ResponseEntity.ok(bookInfo);
        } catch (Exception e) {
            log.error("도서 검색 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /// 도서 수정
    @RequestMapping(value = "/books/{bookId}", method = {RequestMethod.PUT, RequestMethod.POST})
    public String updateBook(HttpServletRequest request,
                             @ModelAttribute BookUpdateRequest req,
                             @PathVariable Long bookId,
                             @RequestParam(value = "tags", required = false) String tags,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images,
                             @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");

        // 태그는 기존 것을 모두 비운 뒤 새로 설정되도록 처리
        if (tags != null && !tags.isBlank()) {
            Set<String> tagSet = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toSet());
            req.setTagNames(tagSet);
        } else {
            req.setTagNames(Collections.emptySet());
        }
        if (req.getIsWrapped() == null) {
            req.setIsWrapped(false);
        }
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            req.setDeleteImageIds(deleteImageIds);
        }

        bookClient.updateBook(token, bookId, req, images);
        return "redirect:/admin/books";
    }

    /// 도서 삭제
    @DeleteMapping("/books/{bookId}")
    public String deleteBook(HttpServletRequest request,
                             @PathVariable Long bookId) {
        String token = "Bearer " + CookieUtils.getCookieValue(request, "accessToken");
        bookClient.deleteBook(token, bookId);
        return "redirect:/admin/books";
    }


    /// 도서 상태변경
    @PatchMapping("/books/{bookId}/status")
    public String updateStatus(HttpServletRequest req, @PathVariable Long bookId,
                               @RequestParam("status") BookStatus status) {
        BookStatusUpdateRequest request = new BookStatusUpdateRequest(status);
        String token = "Bearer " + CookieUtils.getCookieValue(req, "accessToken");

        bookClient.updateBookStatus(token, bookId, request);

        return "redirect:/admin/books";
    }

    /// 도서 이미지 썸네일 설정
    @PostMapping("/books/{bookId}/images/{imageId}/thumbnail")
    public String updateThumbnail(HttpServletRequest req, @PathVariable Long bookId, @PathVariable Long imageId) {
        String token = "Bearer " + CookieUtils.getCookieValue(req, "accessToken");
        bookClient.updateThumbnail(token, bookId, imageId);

        //TODO: 민서가 작성하시오
        return "redirect:/admin/books/update/";
    }

    /// 할인율 변경 요청 (비동기 실행됨)
    @PostMapping("/price/discount")
    public String updateDiscountRate(HttpServletRequest req, @RequestParam("rate") int rate) {
        String token = "Bearer " + CookieUtils.getCookieValue(req, "accessToken");
        bookClient.updateDiscountRate(token, rate);

        //TODO: 민서가 작성하시오
        return "redirect:/";
    }

    /// 가격 상태조회 (프론트에서 10초마다 노출되어야함) 할인율 변경을 했을때 db에서 책 price 전체가 변경되기 전까지 노출되어야함 사실 꼭 10초가 아니어도 되긴해...
    @GetMapping("/price/status")
    public String getUpdateStatus(HttpServletRequest req) {
        String token = "Bearer " + CookieUtils.getCookieValue(req, "accessToken");
        bookClient.getUpdateStatus(token);
        //TODO: 밍서가 작성하시오
        return "redierct://dsaf";
    }

    private String buildTagString(BookDetailResponse book) {
        if (book == null || book.getTags() == null) {
            return "";
        }
        return book.getTags().stream()
                .filter(Objects::nonNull)
                .map(TagDto::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }
}
