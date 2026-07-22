package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.LicenseKey;
import com.yunxingcloud.order.repository.LicenseKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenseKeyServiceTest {

    @Mock private LicenseKeyRepository repo;
    @InjectMocks private LicenseKeyService service;

    @Test
    void shouldGenerateKeys() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var keys = service.generate(100L, 3, 200L);
        assertThat(keys).hasSize(3);
        keys.forEach(k -> {
            assertThat(k.getKeyCode()).contains("-");
            assertThat(k.getStatus()).isEqualTo("0");
        });
        verify(repo, times(3)).save(any());
    }

    @Test
    void shouldActivateKey() {
        LicenseKey k = new LicenseKey(); k.setKeyCode("ABCD-EFGH"); k.setStatus("0");
        when(repo.findByKeyCode("ABCD-EFGH")).thenReturn(Optional.of(k));
        when(repo.save(any())).thenReturn(k);

        LicenseKey result = service.activate("ABCD-EFGH", "user1");
        assertThat(result.getStatus()).isEqualTo("1");
        assertThat(result.getActivatedBy()).isEqualTo("user1");
    }

    @Test
    void shouldRejectAlreadyUsedKey() {
        LicenseKey k = new LicenseKey(); k.setKeyCode("ABCD-EFGH"); k.setStatus("1");
        when(repo.findByKeyCode("ABCD-EFGH")).thenReturn(Optional.of(k));

        assertThatThrownBy(() -> service.activate("ABCD-EFGH", "user1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("已使用");
    }
}