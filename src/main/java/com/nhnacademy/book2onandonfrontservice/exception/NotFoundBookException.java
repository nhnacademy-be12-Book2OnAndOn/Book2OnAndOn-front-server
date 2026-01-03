package com.nhnacademy.book2onandonfrontservice.exception;

// 백엔드에서 체크하면 좋겠지만 관리자는 Book_Delete 상태인 책이 보여야하고 유저는 안보여야돼서 프론트에서 처리해야함
public class NotFoundBookException extends RuntimeException {
    public NotFoundBookException(Long bookId) {
        super("bookId="+bookId+"를 찾을 수 없습니다.");
    }
}
