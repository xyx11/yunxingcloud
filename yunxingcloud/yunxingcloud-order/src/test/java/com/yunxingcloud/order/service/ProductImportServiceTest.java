package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductImportServiceTest {

    @Mock private ProductRepository repo;
    @InjectMocks private ProductImportService service;

    @Test
    void shouldExportCsv() {
        Product p = new Product(); p.setId(1L); p.setName("商品A");
        p.setPrice(9900L); p.setStock(10); p.setSales(100); p.setStatus("0");
        when(repo.findAll()).thenReturn(List.of(p));

        byte[] csv = service.exportCsv();
        String content = new String(csv);
        assertThat(content).contains("ID,名称,描述,价格(元),库存,销量,状态");
        assertThat(content).contains("商品A");
        assertThat(content).contains("99.00");
    }
}