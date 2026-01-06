package util;

import static org.assertj.core.api.Assertions.assertThat;

import com.nhnacademy.book2onandonfrontservice.util.JwtUtils;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUtilsTest {

    private String createTestToken(String payloadJson) {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
        String signature = "dummy-signature";
        return header + "." + payload + "." + signature;
    }

    @Test
    @DisplayName("JWT 토큰에서 유저 아이디 정상 추출 확인")
    void getUserId_ValidToken() {
        String payload = "{\"userId\": 12345, \"role\": \"ROLE_USER\"}";
        String token = createTestToken(payload);

        Long userId = JwtUtils.getUserId(token);

        assertThat(userId).isEqualTo(12345L);
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 입력 시 유저 아이디 null 반환 확인")
    void getUserId_InvalidToken() {
        String token = "invalid.token.format";

        Long userId = JwtUtils.getUserId(token);

        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("페이로드에 userId 필드가 없는 경우 null 반환 확인")
    void getUserId_NoField() {
        String payload = "{\"sub\": \"123\"}";
        String token = createTestToken(payload);

        Long userId = JwtUtils.getUserId(token);

        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("JWT 토큰에서 권한 정보 정상 추출 확인")
    void getRole_ValidToken() {
        String payload = "{\"userId\": 1, \"role\": \"ROLE_ADMIN\"}";
        String token = createTestToken(payload);

        String role = JwtUtils.getRole(token);

        assertThat(role).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 입력 시 권한 정보 null 반환 확인")
    void getRole_InvalidToken() {
        String token = "wrong-token";

        String role = JwtUtils.getRole(token);

        assertThat(role).isNull();
    }

    @Test
    @DisplayName("페이로드에 role 필드가 없는 경우 null 반환 확인")
    void getRole_NoField() {
        String payload = "{\"userId\": 1}";
        String token = createTestToken(payload);

        String role = JwtUtils.getRole(token);

        assertThat(role).isNull();
    }
}