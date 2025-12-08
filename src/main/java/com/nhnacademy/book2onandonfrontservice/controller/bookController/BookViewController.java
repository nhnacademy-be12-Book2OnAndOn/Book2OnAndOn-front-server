package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDetailResponse;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookViewController {

    private final BookClient bookClient;

    /// 메인 페이지 (대시보드)
    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "0") int page, Model model) {
        commonData(model);
        Page<BookDto> response = bookClient.getNewArrivals(null, page,8);
//        List<BookDto> bestsellerDaily = bookClient.getBestsellers("DAILY");
//        List<BookDto> bestsellerWeek = bookClient.getBestsellers("WEEK");
        Page<BookDto> likeBest = bookClient.getPopularBooks(page,0);

        if(response != null){
            model.addAttribute("newBooks", response.getContent());
        }
//        if(bestsellerDaily != null){
//            model.addAttribute("bestDaily", bestsellerDaily);
//        }
//        if(bestsellerWeek != null){
//            model.addAttribute("bestWeek", bestsellerWeek);
//        }
        if(likeBest !=null){
            model.addAttribute("likeBest", likeBest);
        }
        return "dashboard";
    }

    /// 도서 상세조회
    @GetMapping("/books/{bookId}")
    public String getBookDetail(@PathVariable Long bookId, Model model){
        commonData(model);
        BookDetailResponse bookDetail = bookClient.getBookDetail(bookId);
        if(bookDetail!=null){
            model.addAttribute("bookDetail", bookDetail);
        }
        //TODO: 북 상세조회 페이지 만들기
        return "민서가 작성하시오";
    }


    // 공통 데이터 (카테고리, 태그 등) 로딩 헬퍼 메서드
    private void commonData(Model model) {
        model.addAttribute("categories", bookClient.getCategories());
//        model.addAttribute("popularTags", bookClient.getPopularTags());
    }
}
