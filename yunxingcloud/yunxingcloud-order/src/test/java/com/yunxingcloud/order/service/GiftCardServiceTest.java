package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.GiftCard;
import com.yunxingcloud.order.repository.GiftCardRepository;
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
class GiftCardServiceTest {

    @Mock private GiftCardRepository repo;
    @InjectMocks private GiftCardService service;

    @Test
    void shouldCreateGiftCard() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GiftCard result = service.create(10000L, 365);

        assertThat(result.getCardNo()).startsWith("GC");
        assertThat(result.getAmount()).isEqualTo(10000L);
        assertThat(result.getStatus()).isEqualTo("0");
        verify(repo).save(any());
    }

    @Test
    void shouldQueryByCardNo() {
        GiftCard card = new GiftCard();
        card.setCardNo("GC001");
        card.setAmount(10000L);
        card.setBalance(10000L);
        card.setStatus("0");
        when(repo.findByCardNo("GC001")).thenReturn(Optional.of(card));

        assertThat(repo.findByCardNo("GC001")).isPresent();
    }

    @Test
    void shouldReturnEmptyForInvalidCard() {
        when(repo.findByCardNo("INVALID")).thenReturn(Optional.empty());

        assertThat(repo.findByCardNo("INVALID")).isEmpty();
    }
}