package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Merchant;
import com.yunxingcloud.order.repository.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock private MerchantRepository repo;
    @InjectMocks private MerchantService service;

    @Test
    void shouldApply() {
        Merchant m = new Merchant(); m.setShopName("测试店铺"); m.setPhone("13800138000");
        when(repo.findByPhone("13800138000")).thenReturn(null);
        when(repo.save(any())).thenReturn(m);

        Merchant result = service.apply(m);
        assertThat(result.getStatus()).isEqualTo("0");
    }

    @Test
    void shouldRejectDuplicatePhone() {
        Merchant m = new Merchant(); m.setPhone("13800138000");
        when(repo.findByPhone("13800138000")).thenReturn(m);

        assertThatThrownBy(() -> service.apply(m))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("已提交过申请");
    }

    @Test
    void shouldApprove() {
        Merchant m = new Merchant(); m.setId(1L); m.setStatus("0");
        when(repo.findById(1L)).thenReturn(Optional.of(m));
        when(repo.save(any())).thenReturn(m);

        Merchant result = service.approve(1L);
        assertThat(result.getStatus()).isEqualTo("1");
    }

    @Test
    void shouldReject() {
        Merchant m = new Merchant(); m.setId(1L); m.setStatus("0");
        when(repo.findById(1L)).thenReturn(Optional.of(m));
        when(repo.save(any())).thenReturn(m);

        Merchant result = service.reject(1L);
        assertThat(result.getStatus()).isEqualTo("2");
    }

    @Test
    void shouldNotApproveNonPending() {
        Merchant m = new Merchant(); m.setId(1L); m.setStatus("1");
        when(repo.findById(1L)).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.approve(1L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldListByStatus() {
        Merchant m = new Merchant(); m.setId(1L);
        when(repo.findByStatus("0")).thenReturn(List.of(m));

        List<Merchant> result = service.list("0");
        assertThat(result).hasSize(1);
    }
}