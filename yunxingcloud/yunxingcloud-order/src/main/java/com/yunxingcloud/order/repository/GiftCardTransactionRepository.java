package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.GiftCardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GiftCardTransactionRepository extends JpaRepository<GiftCardTransaction, Long> {
    List<GiftCardTransaction> findByCardIdOrderByCreatedAtDesc(Long cardId);
}
