package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByStatusOrderByPublishAtDesc(String status);
    List<Article> findByCategoryAndStatus(String category, String status);
}