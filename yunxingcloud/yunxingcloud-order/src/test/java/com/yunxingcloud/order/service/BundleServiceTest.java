package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.entity.ProductBundle;
import com.yunxingcloud.order.repository.ProductBundleRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BundleServiceTest {

    @Mock private ProductBundleRepository bundleRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private BundleService service;

    @Test
    void shouldListBundles() {
        ProductBundle b = new ProductBundle(); b.setId(1L); b.setName("套餐A");
        when(bundleRepo.findByStatus("1")).thenReturn(List.of(b));

        List<ProductBundle> result = service.list();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("套餐A");
    }

    @Test
    void shouldGetBundleDetail() {
        ProductBundle b = new ProductBundle();
        b.setId(1L); b.setName("套餐A"); b.setBundlePrice(8000L);
        b.setOriginalPrice(12000L); b.setProductIds("1,2");
        when(bundleRepo.findById(1L)).thenReturn(Optional.of(b));
        Product p1 = new Product(); p1.setId(1L); p1.setName("商品1");
        when(productRepo.findById(1L)).thenReturn(Optional.of(p1));
        when(productRepo.findById(2L)).thenReturn(Optional.empty());

        Map<String, Object> result = service.detail(1L);
        assertThat(result).isNotNull();
        assertThat(result.get("savings")).isEqualTo(4000L);
    }

    @Test
    void shouldReturnNullForMissingBundle() {
        when(bundleRepo.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.detail(99L)).isNull();
    }
}