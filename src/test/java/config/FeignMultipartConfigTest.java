package config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.book2onandonfrontservice.config.FeignMultipartConfig;
import com.nhnacademy.book2onandonfrontservice.config.JsonMultipartEncoder;
import feign.codec.Encoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class FeignMultipartConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(FeignMultipartConfig.class)
            .withBean(ObjectFactory.class, () -> mock(ObjectFactory.class))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    @DisplayName("멀티파트 폼 인코더 빈 생성 확인")
    void multipartFormEncoder_BeanCreation() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Encoder.class);
            Encoder encoder = context.getBean(Encoder.class);
            assertThat(encoder).isInstanceOf(JsonMultipartEncoder.class);
        });
    }

    @Test
    @DisplayName("인코더 계층 구조 설정 확인")
    void multipartFormEncoder_HierarchySetup() {
        ObjectFactory<HttpMessageConverters> messageConverters = mock(ObjectFactory.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FeignMultipartConfig config = new FeignMultipartConfig(messageConverters, objectMapper);

        Encoder encoder = config.multipartFormEncoder();

        assertThat(encoder).isNotNull();
        assertThat(encoder).isInstanceOf(JsonMultipartEncoder.class);
    }

    @Test
    @DisplayName("설정 클래스 의존성 주입 확인")
    void configDependency_Injection() {
        contextRunner.run(context -> {
            FeignMultipartConfig config = context.getBean(FeignMultipartConfig.class);
            assertThat(config).isNotNull();
        });
    }
}