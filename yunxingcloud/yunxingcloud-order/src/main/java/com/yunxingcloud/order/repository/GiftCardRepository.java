package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.GiftCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    Optional<GiftCard> findByCardNo(String cardNo);
}