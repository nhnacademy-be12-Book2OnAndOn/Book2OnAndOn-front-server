package com.nhnacademy.book2onandonfrontservice.controller.bookController;

import com.nhnacademy.book2onandonfrontservice.client.BookClient;
import com.nhnacademy.book2onandonfrontservice.dto.bookdto.BookLikeToggleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/front/books")
@RequiredArgsConstructor
public class BookApiController {

    private final BookClient bookClient;

    @PostMapping("/{bookId}/likes")
    public ResponseEntity<?> toggleLike(@PathVariable Long bookId){
        try{
            BookLikeToggleResponse response = bookClient.toggleLike(bookId);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            String msg = e.getMessage();
            log.error("좋아요 요청 실패: {}", msg);

            if(msg!= null && msg.contains("401")){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 처리에 실패했습니다.");
        }
    }
}
