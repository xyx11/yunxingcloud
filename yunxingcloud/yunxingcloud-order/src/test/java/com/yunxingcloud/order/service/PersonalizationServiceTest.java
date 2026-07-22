package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.entity.OrderLine;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import com.yunxingcloud.order.repository.OrderLineRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PersonalizationServiceTest {

    @Mock private OrderHeadRepository orderRepo;
    @Mock private OrderLineRepository lineRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private PersonalizationService service;

    @Test
    void shouldReturnHomeWithHistory() {
        OrderHead o = new OrderHead(); o.setId(1L);
        when(orderRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(List.of(o));
        OrderLine l = new OrderLine(); l.setOrderId(1L); l.setProductId(100L);
        when(lineRepo.findByOrderId(1L)).thenReturn(List.of(l));
        Product p = new Product(); p.setId(100L); p.setCategoryId(5L);
        when(productRepo.findAllById(any())).thenReturn(List.of(p));
        // Stub all other product queries as empty
        lenient().when(productRepo.findByCategoryIdAndStatus(any(), any(), any(PageRequest.class)))
            .thenReturn(List.of());
        lenient().when(productRepo.findByIsHotTrueAndStatus(any(), any(Sort.class)))
            .thenReturn(List.of());
        lenient().when(productRepo.findByIsNewTrueAndStatus(any(), any(Sort.class)))
            .thenReturn(List.of());

        Map<String, Object> result = service.personalizedHome("user1");
        assertThat(result).isNotNull();
        assertThat(result).containsKey("categoryRecommend");
        assertThat(result).containsKey("hot");
    }

    @Test
    void shouldReturnHomeWithoutHistory() {
        when(orderRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(List.of());
        lenient().when(productRepo.findByIsHotTrueAndStatus(any(), any(Sort.class)))
            .thenReturn(List.of());
        lenient().when(productRepo.findByIsNewTrueAndStatus(any(), any(Sort.class)))
            .thenReturn(List.of());

        Map<String, Object> result = service.personalizedHome("user1");
        assertThat(result).isNotNull();
        assertThat(result).containsKey("hot");
        assertThat(result).containsKey("news");
    }
}