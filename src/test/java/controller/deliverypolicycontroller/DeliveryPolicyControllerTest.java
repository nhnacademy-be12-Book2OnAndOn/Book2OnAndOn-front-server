package controller.deliverypolicycontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhnacademy.book2onandonfrontservice.Book2OnAndOnFrontServiceApplication;
import com.nhnacademy.book2onandonfrontservice.client.DeliveryPolicyClient;
import com.nhnacademy.book2onandonfrontservice.controller.GlobalControllerAdvice;
import com.nhnacademy.book2onandonfrontservice.controller.deliveryPolicyController.DeliveryPolicyController;
import com.nhnacademy.book2onandonfrontservice.dto.deliveryDto.DeliveryPolicyDto;
import com.nhnacademy.book2onandonfrontservice.interceptor.AdminInterceptor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = DeliveryPolicyController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalControllerAdvice.class
    )
)
@ContextConfiguration(classes = Book2OnAndOnFrontServiceApplication.class)
@WithMockUser
class DeliveryPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AdminInterceptor adminInterceptor;
    @MockitoBean private DeliveryPolicyClient deliveryPolicyClient;

    @BeforeEach
    void setUp() throws Exception {
        given(adminInterceptor.preHandle(any(), any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("배송 정책 목록 조회 및 리스트 반환 성공")
    void getDeliveryPoliciesSimple_Success() throws Exception {
        DeliveryPolicyDto policy = new DeliveryPolicyDto();
        PageImpl<DeliveryPolicyDto> page = new PageImpl<>(List.of(policy), PageRequest.of(0, 10), 1);
        
        given(deliveryPolicyClient.getDeliveryPolicy()).willReturn(page);

        mockMvc.perform(get("/api/delivery-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
