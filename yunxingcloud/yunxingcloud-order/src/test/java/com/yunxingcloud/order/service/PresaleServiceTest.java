package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Presale;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import com.yunxingcloud.order.repository.PresaleRepository;
import com.yunxingcloud.order.repository.UserAddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresaleServiceTest {

    @Mock private PresaleRepository presaleRepo;
    @Mock private OrderHeadRepository orderRepo;
    @Mock private UserAddressRepository addressRepo;
    @InjectMocks private PresaleService service;

    @Test
    void shouldListPresales() {
        Presale p = new Presale(); p.setId(1L); p.setProductName("预售商品");
        Page<Presale> page = new PageImpl<>(List.of(p));
        when(presaleRepo.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Map<String, Object> result = service.list(0, 10);
        assertThat(result.get("totalElements")).isEqualTo(1L);
    }

    @Test
    void shouldGetPresaleDetail() {
        Presale p = new Presale(); p.setId(1L); p.setProductName("预售商品");
        p.setDepositPrice(5000L);
        when(presaleRepo.findById(1L)).thenReturn(Optional.of(p));

        Optional<Presale> result = service.detail(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getDepositPrice()).isEqualTo(5000L);
    }

    @Test
    void shouldReturnEmptyForMissingPresale() {
        when(presaleRepo.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.detail(99L)).isEmpty();
    }
}