package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Article;
import com.yunxingcloud.order.repository.ArticleRepository;
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
class ArticleServiceTest {

    @Mock private ArticleRepository repo;
    @InjectMocks private ArticleService service;

    @Test
    void shouldListByStatus() {
        Article a = new Article(); a.setId(1L); a.setTitle("文章A");
        when(repo.findByStatusOrderByPublishAtDesc("1")).thenReturn(List.of(a));

        List<Article> result = service.list("1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("文章A");
    }

    @Test
    void shouldListByCategory() {
        when(repo.findByCategoryAndStatus("news", "1")).thenReturn(List.of());

        List<Article> result = service.byCategory("news");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetAndIncrementViewCount() {
        Article a = new Article(); a.setId(1L); a.setStatus("1"); a.setViewCount(0L);
        when(repo.findById(1L)).thenReturn(Optional.of(a));
        when(repo.save(any())).thenReturn(a);

        Optional<Article> result = service.get(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getViewCount()).isEqualTo(1L);
        verify(repo).save(a);
    }

    @Test
    void shouldCreate() {
        Article a = new Article(); a.setTitle("新文章");
        when(repo.save(any())).thenReturn(a);

        Article result = service.create(a);
        assertThat(result.getTitle()).isEqualTo("新文章");
    }
}