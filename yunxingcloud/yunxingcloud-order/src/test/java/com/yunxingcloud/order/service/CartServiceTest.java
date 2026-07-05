package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.CartItem;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.CartItemRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartItemRepository cartRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private CartService cartService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("测试商品");
        sampleProduct.setPrice(9900L);
        sampleProduct.setStock(100);
    }

    @Test
    void shouldReturnItemsWhenCartNotEmpty() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setProductName("商品");
        item.setQuantity(2);
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(item));
        Map<String, Object> result = cartService.list("user1");
        assertThat(result.get("items")).isNotNull();
    }

    @Test
    void shouldReturnRecommendationsWhenCartEmpty() {
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of());
        when(productRepo.findByStatus(eq("0"), any()))
                .thenReturn(List.of(sampleProduct));
        Map<String, Object> result = cartService.list("user1");
        assertThat(result.get("recommended")).isNotNull();
    }

    @Test
    void shouldAddItemToCart() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(cartRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CartItem result = cartService.add("user1", 1L, 2);
        assertThat(result.getProductName()).isEqualTo("测试商品");
        assertThat(result.getPrice()).isEqualTo(9900L);
        assertThat(result.getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.add("user1", 99L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("商品不存在");
    }

    @Test
    void shouldThrowWhenStockInsufficient() {
        sampleProduct.setStock(0);
        when(productRepo.findById(1L)).thenReturn(Optional.of(sampleProduct));
        assertThatThrownBy(() -> cartService.add("user1", 1L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("库存不足");
    }

    @Test
    void shouldRemoveOwnItem() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUsername("user1");
        when(cartRepo.findById(1L)).thenReturn(Optional.of(item));
        cartService.remove(1L, "user1");
        verify(cartRepo).delete(item);
    }

    @Test
    void shouldNotRemoveOtherUserItem() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUsername("other_user");
        when(cartRepo.findById(1L)).thenReturn(Optional.of(item));
        cartService.remove(1L, "user1");
        verify(cartRepo, never()).delete(any());
    }

    @Test
    void shouldClearCart() {
        cartService.clear("user1");
        verify(cartRepo).deleteByUsername("user1");
    }
}
