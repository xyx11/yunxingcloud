package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.ShareRecord;
import com.yunxingcloud.order.entity.Wishlist;
import com.yunxingcloud.order.repository.ShareRecordRepository;
import com.yunxingcloud.order.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SocialServiceTest {

    @Mock private WishlistRepository wishRepo;
    @Mock private ShareRecordRepository shareRepo;
    @InjectMocks private SocialService service;

    @BeforeEach
    void setUp() {
        when(wishRepo.existsByUsernameAndProductId(anyString(), anyLong())).thenReturn(false);
    }

    @Test
    void shouldListWishlist() {
        Wishlist w = new Wishlist(); w.setId(1L); w.setUsername("user1"); w.setProductId(100L);
        when(wishRepo.findByUsername("user1")).thenReturn(List.of(w));

        List<Wishlist> result = service.wishlist("user1");
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldAddToWishlist() {
        Wishlist w = new Wishlist(); w.setId(1L); w.setUsername("user1"); w.setProductId(100L);
        when(wishRepo.save(any())).thenReturn(w);

        Wishlist result = service.addWish("user1", 100L);
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(100L);
        verify(wishRepo).save(any());
    }

    @Test
    void shouldNotAddDuplicateWish() {
        when(wishRepo.existsByUsernameAndProductId("user1", 100L)).thenReturn(true);

        Wishlist result = service.addWish("user1", 100L);
        assertThat(result).isNull();
        verify(wishRepo, never()).save(any());
    }

    @Test
    void shouldRemoveWish() {
        service.removeWish("user1", 100L);
        verify(wishRepo).deleteByUsernameAndProductId("user1", 100L);
    }

    @Test
    void shouldShareProduct() {
        ShareRecord sr = new ShareRecord(); sr.setId(1L); sr.setSharer("user1");
        sr.setProductId(100L); sr.setChannel("wechat");
        when(shareRepo.save(any())).thenReturn(sr);

        ShareRecord result = service.share("user1", 100L, "wechat");
        assertThat(result.getChannel()).isEqualTo("wechat");
        verify(shareRepo).save(any());
    }

    @Test
    void shouldTrackClick() {
        ShareRecord sr = new ShareRecord(); sr.setId(1L); sr.setClickCount(0L);
        when(shareRepo.findById(1L)).thenReturn(Optional.of(sr));

        service.click(1L);

        verify(shareRepo).save(argThat(s -> s.getClickCount() == 1L));
    }
}