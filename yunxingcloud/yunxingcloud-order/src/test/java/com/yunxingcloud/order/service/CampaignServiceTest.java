package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Campaign;
import com.yunxingcloud.order.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock private CampaignRepository repo;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;
    @InjectMocks private CampaignService campaignService;

    private Campaign fullRedCampaign;
    private Campaign discountCampaign;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        lenient().when(redis.opsForValue()).thenReturn(valueOps);

        fullRedCampaign = new Campaign();
        fullRedCampaign.setId(1L);
        fullRedCampaign.setType("full_reduction");
        fullRedCampaign.setThreshold(10000L); // ¥100
        fullRedCampaign.setDiscount(2000L);   // ¥20 off
        fullRedCampaign.setStatus("1");
        fullRedCampaign.setStartTime(LocalDateTime.now().minusDays(1));
        fullRedCampaign.setEndTime(LocalDateTime.now().plusDays(1));
        fullRedCampaign.setLimitPerUser(3);

        discountCampaign = new Campaign();
        discountCampaign.setId(2L);
        discountCampaign.setType("discount");
        discountCampaign.setDiscount(10L);      // 10% off
        discountCampaign.setMaxDiscount(5000L); // max ¥50
        discountCampaign.setStatus("1");
        discountCampaign.setStartTime(LocalDateTime.now().minusDays(1));
        discountCampaign.setEndTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void shouldCalculateFullReductionWhenMeetThreshold() {
        when(repo.findById(1L)).thenReturn(Optional.of(fullRedCampaign));
        when(valueOps.increment(anyString())).thenReturn(1L);

        long discount = campaignService.calculateDiscount(1L, 15000L, "user1");

        assertThat(discount).isEqualTo(2000L); // ¥20 off
    }

    @Test
    void shouldReturnZeroWhenBelowThreshold() {
        fullRedCampaign.setThreshold(20000L);
        when(repo.findById(1L)).thenReturn(Optional.of(fullRedCampaign));
        when(valueOps.increment(anyString())).thenReturn(1L);

        long discount = campaignService.calculateDiscount(1L, 10000L, "user1");

        assertThat(discount).isEqualTo(0);
    }

    @Test
    void shouldCalculateDiscountRate() {
        when(repo.findById(2L)).thenReturn(Optional.of(discountCampaign));
        when(valueOps.increment(anyString())).thenReturn(1L);

        long discount = campaignService.calculateDiscount(2L, 30000L, "user1");

        assertThat(discount).isEqualTo(3000L); // 10% of ¥300 = ¥30
    }

    @Test
    void shouldCapDiscountAtMaxDiscount() {
        when(repo.findById(2L)).thenReturn(Optional.of(discountCampaign));
        when(valueOps.increment(anyString())).thenReturn(1L);

        long discount = campaignService.calculateDiscount(2L, 100000L, "user1");

        assertThat(discount).isEqualTo(5000L); // capped at max ¥50
    }

    @Test
    void shouldRejectWhenCampaignNotActive() {
        fullRedCampaign.setStatus("0");
        when(repo.findById(1L)).thenReturn(Optional.of(fullRedCampaign));

        assertThatThrownBy(() -> campaignService.calculateDiscount(1L, 10000L, "user1"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectWhenOutOfTime() {
        fullRedCampaign.setStartTime(LocalDateTime.now().plusDays(1));
        when(repo.findById(1L)).thenReturn(Optional.of(fullRedCampaign));

        assertThatThrownBy(() -> campaignService.calculateDiscount(1L, 10000L, "user1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("不在活动时间");
    }

    @Test
    void shouldRejectWhenExceedLimitPerUser() {
        when(repo.findById(1L)).thenReturn(Optional.of(fullRedCampaign));
        when(valueOps.increment(anyString())).thenReturn(4L); // exceeds limit of 3

        assertThatThrownBy(() -> campaignService.calculateDiscount(1L, 10000L, "user1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已达参与次数上限");
    }

    @Test
    void shouldRejectWhenStockExhausted() {
        fullRedCampaign.setTotalStock(100);
        fullRedCampaign.setUsedCount(100);
        when(repo.findById(1L)).thenReturn(Optional.of(fullRedCampaign));
        when(valueOps.increment(anyString())).thenReturn(1L);

        assertThatThrownBy(() -> campaignService.calculateDiscount(1L, 10000L, "user1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已抢完");
    }
}