package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.MemberTier;
import com.yunxingcloud.order.entity.PointsAccount;
import com.yunxingcloud.order.repository.MemberTierRepository;
import com.yunxingcloud.order.repository.PointsAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberServiceTest {

    @Mock private PointsAccountRepository pointsRepo;
    @Mock private MemberTierRepository tierRepo;
    @InjectMocks private MemberService service;

    private MemberTier silver, gold;

    @BeforeEach
    void setUp() {
        silver = new MemberTier(); silver.setId(1L); silver.setName("银卡");
        silver.setMinPoints(100L); silver.setDiscountRate(95);

        gold = new MemberTier(); gold.setId(2L); gold.setName("金卡");
        gold.setMinPoints(500L); gold.setDiscountRate(90);
    }

    @Test
    void shouldGetTierByTotalEarned() {
        PointsAccount acc = new PointsAccount();
        acc.setUsername("user1"); acc.setBalance(600L); acc.setTotalEarned(600L);
        when(pointsRepo.findByUsername("user1")).thenReturn(Optional.of(acc));
        when(tierRepo.findAllByOrderByMinPointsDesc()).thenReturn(List.of(gold, silver));

        MemberTier result = service.getTier("user1");
        assertThat(result.getName()).isEqualTo("金卡");
    }

    @Test
    void shouldReturnNullWhenNoTierMatch() {
        PointsAccount acc = new PointsAccount();
        acc.setUsername("user1"); acc.setTotalEarned(0L);
        when(pointsRepo.findByUsername("user1")).thenReturn(Optional.of(acc));
        when(tierRepo.findAllByOrderByMinPointsDesc()).thenReturn(List.of(gold, silver));

        MemberTier result = service.getTier("user1");
        assertThat(result).isNull();
    }

    @Test
    void shouldGetDiscountRate() {
        PointsAccount acc = new PointsAccount();
        acc.setUsername("user1"); acc.setTotalEarned(600L);
        when(pointsRepo.findByUsername("user1")).thenReturn(Optional.of(acc));
        when(tierRepo.findAllByOrderByMinPointsDesc()).thenReturn(List.of(gold, silver));

        int rate = service.getDiscountRate("user1");
        assertThat(rate).isEqualTo(90);
    }

    @Test
    void shouldGetBenefits() {
        PointsAccount acc = new PointsAccount();
        acc.setUsername("user1"); acc.setTotalEarned(600L); acc.setBalance(200L);
        when(pointsRepo.findByUsername("user1")).thenReturn(Optional.of(acc));
        when(tierRepo.findAllByOrderByMinPointsDesc()).thenReturn(List.of(gold, silver));
        when(tierRepo.findAllByOrderByMinPointsAsc()).thenReturn(List.of(silver, gold));

        Map<String, Object> result = service.benefits("user1");
        assertThat(result.get("currentTier")).isEqualTo("金卡");
        assertThat(result.get("discountRate")).isEqualTo(90);
    }
}