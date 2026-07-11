package com.yunxingcloud.order.service;

import com.yunxingcloud.order.dto.TagDTO;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.entity.ProductTag;
import com.yunxingcloud.order.entity.ProductTagRelation;
import com.yunxingcloud.order.repository.ProductRepository;
import com.yunxingcloud.order.repository.ProductTagRelationRepository;
import com.yunxingcloud.order.repository.ProductTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock private ProductTagRepository tagRepo;
    @Mock private ProductTagRelationRepository relationRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private TagService tagService;

    @Test
    void shouldListTags() {
        ProductTag tag1 = new ProductTag();
        tag1.setId(1L);
        tag1.setName("热卖");
        ProductTag tag2 = new ProductTag();
        tag2.setId(2L);
        tag2.setName("新品");
        when(tagRepo.findAllByOrderBySortOrderAsc()).thenReturn(List.of(tag1, tag2));

        List<ProductTag> result = tagService.list();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("热卖");
        assertThat(result.get(1).getName()).isEqualTo("新品");
    }

    @Test
    void shouldCreateTag() {
        TagDTO dto = new TagDTO();
        dto.setName("包邮");

        ProductTag saved = new ProductTag();
        saved.setId(1L);
        saved.setName("包邮");
        when(tagRepo.save(any())).thenReturn(saved);

        ProductTag result = tagService.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("包邮");
    }

    @Test
    void shouldReturnTagsOfProduct() {
        ProductTagRelation rel = new ProductTagRelation();
        rel.setId(1L);
        rel.setProductId(100L);
        rel.setTagId(10L);
        when(relationRepo.findByProductId(100L)).thenReturn(List.of(rel));

        ProductTag tag = new ProductTag();
        tag.setId(10L);
        tag.setName("热卖");
        when(tagRepo.findById(10L)).thenReturn(Optional.of(tag));

        List<ProductTag> result = tagService.tagsOfProduct(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("热卖");
    }

    @Test
    void shouldReturnEmptyTagsWhenProductHasNoTags() {
        when(relationRepo.findByProductId(999L)).thenReturn(List.of());

        List<ProductTag> result = tagService.tagsOfProduct(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldAddTagToProduct() {
        when(relationRepo.existsByProductIdAndTagId(100L, 10L)).thenReturn(false);

        ProductTagRelation saved = new ProductTagRelation();
        saved.setId(1L);
        saved.setProductId(100L);
        saved.setTagId(10L);
        when(relationRepo.save(any())).thenReturn(saved);

        ProductTagRelation result = tagService.addTag(100L, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(100L);
        assertThat(result.getTagId()).isEqualTo(10L);
    }

    @Test
    void shouldReturnNullWhenAddExistingTag() {
        when(relationRepo.existsByProductIdAndTagId(100L, 10L)).thenReturn(true);

        ProductTagRelation result = tagService.addTag(100L, 10L);

        assertThat(result).isNull();
        verify(relationRepo, never()).save(any());
    }

    @Test
    void shouldRemoveTagFromProduct() {
        tagService.removeTag(100L, 10L);

        verify(relationRepo).deleteByProductIdAndTagId(100L, 10L);
    }

    @Test
    void shouldReturnProductsByTag() {
        ProductTagRelation rel = new ProductTagRelation();
        rel.setId(1L);
        rel.setTagId(10L);
        rel.setProductId(100L);
        when(relationRepo.findByTagId(10L)).thenReturn(List.of(rel));

        Product product = new Product();
        product.setId(100L);
        product.setName("测试商品");
        product.setPrice(9900L);
        when(productRepo.findById(100L)).thenReturn(Optional.of(product));

        List<Product> result = tagService.productsByTag(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("测试商品");
    }

    @Test
    void shouldReturnEmptyWhenNoProductsForTag() {
        when(relationRepo.findByTagId(999L)).thenReturn(List.of());

        List<Product> result = tagService.productsByTag(999L);

        assertThat(result).isEmpty();
    }
}
