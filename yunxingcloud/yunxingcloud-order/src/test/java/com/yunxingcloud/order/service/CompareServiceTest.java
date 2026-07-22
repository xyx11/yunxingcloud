package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.CompareList;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.CompareListRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompareServiceTest {

    @Mock private CompareListRepository compareRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private CompareService service;

    @Test
    void shouldAddToCompare() {
        when(compareRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(List.of());
        CompareList saved = new CompareList(); saved.setId(1L); saved.setProductId(100L);
        when(compareRepo.save(any())).thenReturn(saved);

        CompareList result = service.add("user1", 100L);
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(100L);
    }

    @Test
    void shouldRejectWhenMaxItemsReached() {
        CompareList c = new CompareList(); c.setProductId(1L);
        when(compareRepo.findByUsernameOrderByCreatedAtDesc("user1"))
            .thenReturn(List.of(c, c, c, c));

        assertThatThrownBy(() -> service.add("user1", 100L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("最多对比4个");
    }

    @Test
    void shouldListCompareProducts() {
        CompareList c = new CompareList(); c.setProductId(100L);
        when(compareRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(List.of(c));
        Product p = new Product(); p.setId(100L); p.setName("商品A"); p.setPrice(9900L);
        when(productRepo.findAllById(any())).thenReturn(List.of(p));

        List<Map<String, Object>> result = service.list("user1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("name")).isEqualTo("商品A");
    }

    @Test
    void shouldRemoveFromCompare() {
        service.remove("user1", 100L);
        verify(compareRepo).deleteByUsernameAndProductId("user1", 100L);
    }

    @Test
    void shouldClearCompare() {
        service.clear("user1");
        verify(compareRepo).deleteByUsername("user1");
    }
}