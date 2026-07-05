package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Article;
import com.yunxingcloud.order.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository repo;

    public ArticleService(ArticleRepository repo) { this.repo = repo; }

    public List<Article> list(String status) {
        return repo.findByStatusOrderByPublishAtDesc(status);
    }

    public List<Article> byCategory(String category) {
        return repo.findByCategoryAndStatus(category, "1");
    }

    @Transactional
    public Optional<Article> get(Long id) {
        Article a = repo.findById(id).orElse(null);
        if (a != null && "1".equals(a.getStatus())) {
            a.setViewCount(a.getViewCount() + 1);
            repo.save(a);
        }
        return Optional.ofNullable(a);
    }

    @Transactional
    public Article create(Article article) { return repo.save(article); }

    @Transactional
    public Article update(Long id, Article article) { article.setId(id); return repo.save(article); }
}
