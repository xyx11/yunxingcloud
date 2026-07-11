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
import org.springframework.data.domain.PageRequest;

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
        sampleProduct.setStock(10);
    }

    @Test
    void shouldListCartItems() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setProductName("商品");
        item.setQuantity(2);
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(item));

        Map<String, Object> result = cartService.list("user1");

        assertThat(result.get("items")).isNotNull();
        assertThat(result).doesNotContainKey("recommended");
    }

    @Test
    void shouldListCartWithRecommendations() {
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of());
        Product rec1 = createProduct(1L, "推荐1");
        Product rec2 = createProduct(2L, "推荐2");
        Product rec3 = createProduct(3L, "推荐3");
        Product rec4 = createProduct(4L, "推荐4");
        when(productRepo.findByStatus(eq("0"), any(PageRequest.class)))
                .thenReturn(List.of(rec1, rec2, rec3, rec4));

        Map<String, Object> result = cartService.list("user1");

        assertThat(result.get("recommended")).isNotNull();
        assertThat((List<?>) result.get("recommended")).hasSize(4);
    }

    private Product createProduct(Long id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        return p;
    }

    @Test
    void shouldAddItemToCart() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(cartRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartItem result = cartService.add("user1", 1L, 2);

        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("测试商品");
        assertThat(result.getPrice()).isEqualTo(9900L);
        assertThat(result.getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldThrowWhenAddNonExistentProduct() {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.add("user1", 99L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("商品不存在");
    }

    @Test
    void shouldThrowWhenAddOutOfStock() {
        sampleProduct.setStock(0);
        when(productRepo.findById(1L)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> cartService.add("user1", 1L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("库存不足");
    }

    @Test
    void shouldRemoveCartItem() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUsername("user1");
        when(cartRepo.findById(1L)).thenReturn(Optional.of(item));

        cartService.remove(1L, "user1");

        verify(cartRepo).delete(item);
    }

    @Test
    void shouldNotRemoveOtherUsersItem() {
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
