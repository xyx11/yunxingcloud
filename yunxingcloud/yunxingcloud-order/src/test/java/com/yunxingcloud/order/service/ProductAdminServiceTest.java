package com.yunxingcloud.order.service;

import com.yunxingcloud.order.dto.ProductDTO;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.ProductRepository;
import com.yunxingcloud.order.repository.ProductReviewRepository;
import com.yunxingcloud.order.repository.ProductSkuRepository;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductAdminServiceTest {

    @Mock private ProductRepository repo;
    @Mock private ProductSkuRepository skuRepo;
    @Mock private ProductReviewRepository reviewRepo;
    @Mock private Page<Product> mockPage;
    @InjectMocks private ProductAdminService productAdminService;

    /* ============== list() ============== */

    @Test
    void shouldListProductsWithFilters() {
        PageRequest expected = PageRequest.of(0, 10, Sort.by("price"));
        when(repo.findAll(any(Specification.class), eq(expected)))
                .thenReturn(mockPage);

        Page<Product> result = productAdminService.list(1L, 2L, 1000L, 50000L, "price_asc", 0, 10);

        assertThat(result).isEqualTo(mockPage);
    }

    @Test
    void shouldListProductsWithPriceDescSort() {
        PageRequest expected = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price"));
        when(repo.findAll(any(Specification.class), eq(expected)))
                .thenReturn(mockPage);

        assertThat(productAdminService.list(null, null, null, null, "price_desc", 0, 10))
                .isEqualTo(mockPage);
    }

    @Test
    void shouldListProductsWithSalesSort() {
        PageRequest expected = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "sales"));
        when(repo.findAll(any(Specification.class), eq(expected)))
                .thenReturn(mockPage);

        assertThat(productAdminService.list(null, null, null, null, "sales", 0, 10))
                .isEqualTo(mockPage);
    }

    @Test
    void shouldListProductsWithDefaultSort() {
        PageRequest expected = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(repo.findAll(any(Specification.class), eq(expected)))
                .thenReturn(mockPage);

        assertThat(productAdminService.list(null, null, null, null, "unknown_sort", 0, 10))
                .isEqualTo(mockPage);
    }

    @Test
    void shouldListProductsBuildSpecWithCategoryAndBrand() throws Exception {
        ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.captor();
        when(repo.findAll(specCaptor.capture(), any(PageRequest.class)))
                .thenReturn(mockPage);

        // Execute
        productAdminService.list(1L, 2L, null, null, null, 0, 10);

        // Verify the Specification builds correct predicates
        Specification<Product> spec = specCaptor.getValue();
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate eqStatus = mock(Predicate.class);
        Predicate eqCategory = mock(Predicate.class);
        Predicate eqBrand = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(cb.equal(root.get("status"), "0")).thenReturn(eqStatus);
        when(cb.equal(root.get("categoryId"), 1L)).thenReturn(eqCategory);
        when(cb.equal(root.get("brandId"), 2L)).thenReturn(eqBrand);
        when(cb.and(any(jakarta.persistence.criteria.Predicate[].class))).thenReturn(andPredicate);

        spec.toPredicate(root, query, cb);

        verify(cb).equal(root.get("status"), "0");
        verify(cb).equal(root.get("categoryId"), 1L);
        verify(cb).equal(root.get("brandId"), 2L);
        verify(cb).and(any(jakarta.persistence.criteria.Predicate[].class));
    }

    @Test
    void shouldListProductsBuildSpecWithPriceRange() throws Exception {
        ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.captor();
        when(repo.findAll(specCaptor.capture(), any(PageRequest.class)))
                .thenReturn(mockPage);

        productAdminService.list(null, null, 1000L, 5000L, null, 0, 10);

        Specification<Product> spec = specCaptor.getValue();
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate eqStatus = mock(Predicate.class);
        Predicate gePrice = mock(Predicate.class);
        Predicate lePrice = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(cb.equal(root.get("status"), "0")).thenReturn(eqStatus);
        when(cb.ge(root.get("price"), 1000L)).thenReturn(gePrice);
        when(cb.le(root.get("price"), 5000L)).thenReturn(lePrice);
        when(cb.and(any(jakarta.persistence.criteria.Predicate[].class))).thenReturn(andPredicate);

        spec.toPredicate(root, query, cb);

        verify(cb).ge(root.get("price"), 1000L);
        verify(cb).le(root.get("price"), 5000L);
    }

    /* ============== search() ============== */

    @Test
    void shouldSearchProducts() {
        PageRequest expected = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "sales"));
        when(repo.findAll(any(Specification.class), eq(expected)))
                .thenReturn(mockPage);

        assertThat(productAdminService.search("手机", null, null, null, 0, 10))
                .isEqualTo(mockPage);
    }

    @Test
    void shouldSearchProductsWithCategoryAndPrice() {
        PageRequest expected = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "sales"));
        when(repo.findAll(any(Specification.class), eq(expected)))
                .thenReturn(mockPage);

        assertThat(productAdminService.search(null, 1L, 1000L, 5000L, 0, 10))
                .isEqualTo(mockPage);
    }

    @Test
    void shouldSearchProductsBuildSpecWithNameLike() throws Exception {
        ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.captor();
        when(repo.findAll(specCaptor.capture(), any(PageRequest.class)))
                .thenReturn(mockPage);

        productAdminService.search("手机", null, null, null, 0, 10);

        Specification<Product> spec = specCaptor.getValue();
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate eqStatus = mock(Predicate.class);
        Predicate likeName = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(cb.equal(root.get("status"), "0")).thenReturn(eqStatus);
        when(cb.like(root.get("name"), "%手机%")).thenReturn(likeName);
        when(cb.and(any(jakarta.persistence.criteria.Predicate[].class))).thenReturn(andPredicate);

        spec.toPredicate(root, query, cb);

        verify(cb).equal(root.get("status"), "0");
        verify(cb).like(root.get("name"), "%手机%");
    }

    @Test
    void shouldSearchProductsBuildSpecWithAllFilters() throws Exception {
        ArgumentCaptor<Specification<Product>> specCaptor = ArgumentCaptor.captor();
        when(repo.findAll(specCaptor.capture(), any(PageRequest.class)))
                .thenReturn(mockPage);

        productAdminService.search("手机", 1L, 1000L, 5000L, 0, 10);

        Specification<Product> spec = specCaptor.getValue();
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate eqStatus = mock(Predicate.class);
        Predicate likeName = mock(Predicate.class);
        Predicate eqCategory = mock(Predicate.class);
        Predicate gePrice = mock(Predicate.class);
        Predicate lePrice = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(cb.equal(root.get("status"), "0")).thenReturn(eqStatus);
        when(cb.like(root.get("name"), "%手机%")).thenReturn(likeName);
        when(cb.equal(root.get("categoryId"), 1L)).thenReturn(eqCategory);
        when(cb.ge(root.get("price"), 1000L)).thenReturn(gePrice);
        when(cb.le(root.get("price"), 5000L)).thenReturn(lePrice);
        when(cb.and(any(jakarta.persistence.criteria.Predicate[].class))).thenReturn(andPredicate);

        spec.toPredicate(root, query, cb);

        verify(cb).like(root.get("name"), "%手机%");
        verify(cb).equal(root.get("categoryId"), 1L);
        verify(cb).ge(root.get("price"), 1000L);
        verify(cb).le(root.get("price"), 5000L);
    }

    /* ============== hot / newArrivals / get / detail / related ============== */

    @Test
    void shouldReturnHotProducts() {
        List<Product> mockList = List.of();
        when(repo.findByIsHotTrueAndStatus(eq("0"), any(Sort.class)))
                .thenReturn(mockList);

        assertThat(productAdminService.hot()).isSameAs(mockList);
    }

    @Test
    void shouldReturnNewArrivals() {
        List<Product> mockList = List.of();
        when(repo.findByIsNewTrueAndStatus(eq("0"), any(Sort.class)))
                .thenReturn(mockList);

        assertThat(productAdminService.newArrivals()).isSameAs(mockList);
    }

    @Test
    void shouldGetProductById() {
        Product p = new Product();
        p.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        assertThat(productAdminService.get(1L)).isPresent().get()
                .extracting(Product::getId).isEqualTo(1L);
    }

    @Test
    void shouldReturnEmptyWhenGetNonExistent() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThat(productAdminService.get(99L)).isNotPresent();
    }

    @Test
    void shouldReturnNullDetailWhenProductNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThat(productAdminService.detail(99L)).isNull();
    }

    @Test
    void shouldReturnProductDetail() {
        Product p = new Product();
        p.setId(1L);
        p.setCategoryId(10L);
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(skuRepo.findByProductId(1L)).thenReturn(List.of());
        when(reviewRepo.findByProductIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(repo.findByCategoryIdAndIdNot(eq(10L), eq(1L), any(PageRequest.class)))
                .thenReturn(List.of());

        Map<String, Object> detail = productAdminService.detail(1L);

        assertThat(detail).containsKeys("product", "skus", "reviews", "related");
    }

    @Test
    void shouldReturnEmptyRelatedListWhenProductNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThat(productAdminService.related(99L)).isEmpty();
    }

    @Test
    void shouldReturnRelatedProducts() {
        Product p = new Product();
        p.setId(1L);
        p.setCategoryId(10L);
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.findByCategoryIdAndIdNot(eq(10L), eq(1L), any(PageRequest.class)))
                .thenReturn(new java.util.ArrayList<>());
        when(repo.findByStatus(eq("0"), any(PageRequest.class)))
                .thenReturn(List.of(new Product() {{ setId(2L); setName("Test"); setPrice(100L); }}));

        assertThat(productAdminService.related(1L)).isNotEmpty();
    }

    /* ============== create / update / delete ============== */

    @Test
    void shouldCreateProductFromDto() {
        ProductDTO dto = new ProductDTO();
        dto.setName("新商品");
        dto.setPrice(19900L);
        dto.setStock(50);

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("新商品");
        saved.setPrice(19900L);
        saved.setStock(50);
        when(repo.save(any())).thenReturn(saved);

        Product result = productAdminService.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("新商品");
    }

    @Test
    void shouldUpdateProduct() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setName("旧名称");

        Product body = new Product();
        body.setName("新名称");
        body.setPrice(29900L);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Product> result = productAdminService.update(1L, body);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("新名称");
        assertThat(result.get().getPrice()).isEqualTo(29900L);
    }

    @Test
    void shouldReturnEmptyWhenUpdateNonExistent() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThat(productAdminService.update(99L, new Product())).isNotPresent();
    }

    @Test
    void shouldDeleteProduct() {
        when(repo.existsById(1L)).thenReturn(true);

        assertThat(productAdminService.delete(1L)).isTrue();
        verify(repo).deleteById(1L);
    }

    @Test
    void shouldReturnFalseWhenDeleteNonExistent() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThat(productAdminService.delete(99L)).isFalse();
        verify(repo, never()).deleteById(any());
    }
}
