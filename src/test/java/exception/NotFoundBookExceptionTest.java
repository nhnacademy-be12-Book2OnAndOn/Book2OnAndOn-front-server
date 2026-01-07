
package exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nhnacademy.book2onandonfrontservice.exception.NotFoundBookException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotFoundBookExceptionTest {

    @Test
    @DisplayName("예외 발생 시 전달된 bookId를 포함한 정확한 메시지가 생성되어야 한다")
    void exceptionMessageTest() {
        Long bookId = 12345L;
        String expectedMessage = "bookId=12345를 찾을 수 없습니다.";

        NotFoundBookException exception = new NotFoundBookException(bookId);

        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("RuntimeException을 상속받았는지 확인")
    void inheritanceTest() {
        NotFoundBookException exception = new NotFoundBookException(1L);
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("실제 throw 발생 시 예외 타입 검증")
    void throwTest() {
        assertThrows(NotFoundBookException.class, () -> {
            throw new NotFoundBookException(999L);
        });
    }
}