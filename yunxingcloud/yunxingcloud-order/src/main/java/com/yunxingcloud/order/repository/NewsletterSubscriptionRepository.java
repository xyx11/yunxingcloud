package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
    Optional<NewsletterSubscription> findByEmail(String email);
}
