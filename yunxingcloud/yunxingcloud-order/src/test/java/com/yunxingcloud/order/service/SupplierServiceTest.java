package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Supplier;
import com.yunxingcloud.order.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock private SupplierRepository repo;
    @InjectMocks private SupplierService service;

    @Test
    void shouldListSuppliers() {
        Supplier s = new Supplier(); s.setId(1L); s.setName("供应商A");
        when(repo.findByStatus("1")).thenReturn(List.of(s));

        List<Supplier> result = service.list();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("供应商A");
    }

    @Test
    void shouldCreateSupplier() {
        Supplier s = new Supplier(); s.setName("供应商A");
        when(repo.save(any())).thenReturn(s);

        Supplier result = service.create(s);
        assertThat(result.getName()).isEqualTo("供应商A");
    }

    @Test
    void shouldUpdateSupplier() {
        Supplier s = new Supplier(); s.setName("供应商B");
        when(repo.save(any())).thenReturn(s);

        Supplier result = service.update(1L, s);
        assertThat(result.getId()).isEqualTo(1L);
        verify(repo).save(s);
    }
}