package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.GiftCard;
import com.yunxingcloud.order.entity.GiftCardTransaction;
import com.yunxingcloud.order.repository.GiftCardRepository;
import com.yunxingcloud.order.repository.GiftCardTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GiftCardService {

    private final GiftCardRepository repo;
    private final GiftCardTransactionRepository txnRepo;

    public GiftCardService(GiftCardRepository repo, GiftCardTransactionRepository txnRepo) {
        this.repo = repo; this.txnRepo = txnRepo;
    }

    private void recordTxn(Long cardId, String cardNo, String type, Long amount, String remark) {
        GiftCardTransaction t = new GiftCardTransaction();
        t.setCardId(cardId); t.setCardNo(cardNo); t.setType(type);
        t.setAmount(amount); t.setRemark(remark);
        txnRepo.save(t);
    }

    /** 生成礼品卡 */
    public GiftCard create(Long amount, int expireDays) {
        GiftCard card = new GiftCard();
        card.setCardNo("GC" + generateCode());
        card.setAmount(amount);
        card.setBalance(amount);
        card.setStatus("0");
        card.setExpireAt(LocalDateTime.now().plusDays(expireDays));
        return repo.save(card);
    }

    /** 激活 (绑定用户) */
    @Transactional
    public GiftCard activate(String cardNo, String username) {
        GiftCard card = repo.findByCardNo(cardNo)
                .orElseThrow(() -> new IllegalArgumentException("礼品卡不存在"));
        if (!"0".equals(card.getStatus())) throw new IllegalStateException("礼品卡已激活或已过期");
        if (card.getExpireAt().isBefore(LocalDateTime.now())) {
            card.setStatus("3"); repo.save(card);
            throw new IllegalStateException("礼品卡已过期");
        }
        card.setStatus("1"); card.setOwner(username); card.setActivateAt(LocalDateTime.now());
        repo.save(card);
        recordTxn(card.getId(), card.getCardNo(), "ACTIVATE", card.getAmount(), "激活礼品卡");
        return card;
    }

    /** 使用礼品卡支付 (返回抵扣金额) */
    @Transactional
    public long pay(String cardNo, Long orderAmount) {
        GiftCard card = repo.findByCardNo(cardNo).orElseThrow();
        if (!"1".equals(card.getStatus())) throw new IllegalStateException("礼品卡不可用");
        long deduct = Math.min(card.getBalance(), orderAmount);
        card.setBalance(card.getBalance() - deduct);
        if (card.getBalance() <= 0) card.setStatus("2");
        repo.save(card);
        recordTxn(card.getId(), card.getCardNo(), "PAY", deduct, "礼品卡支付");
        return deduct;
    }

    public GiftCard query(String cardNo) {
        return repo.findByCardNo(cardNo).orElse(null);
    }

    /** 获取用户所有礼品卡 */
    public java.util.List<GiftCard> myCards(String username) {
        return repo.findAll().stream()
            .filter(c -> username.equals(c.getOwner()))
            .toList();
    }

    /** 批量失效过期卡 */
    @Transactional
    public int expireOverdueCards() {
        int count = 0;
        var cards = repo.findAll();
        for (var c : cards) {
            if (c.getExpireAt() != null && c.getExpireAt().isBefore(java.time.LocalDateTime.now())
                    && !"3".equals(c.getStatus()) && !"2".equals(c.getStatus())) {
                c.setStatus("3");
                repo.save(c);
                count++;
            }
        }
        return count;
    }

    /** 用户购买礼品卡 */
    @Transactional
    public GiftCard purchase(String username, Long amount) {
        GiftCard card = new GiftCard();
        card.setCardNo("GC" + generateCode());
        card.setAmount(amount);
        card.setBalance(amount);
        card.setStatus("1");
        card.setOwner(username);
        card.setActivateAt(LocalDateTime.now());
        card.setExpireAt(LocalDateTime.now().plusDays(365));
        repo.save(card);
        recordTxn(card.getId(), card.getCardNo(), "PURCHASE", amount, "购买礼品卡");
        return card;
    }

    /** 获取礼品卡交易记录 */
    public List<GiftCardTransaction> getTransactions(Long cardId) {
        return txnRepo.findByCardIdOrderByCreatedAtDesc(cardId);
    }

    private String generateCode() {
        return String.format("%016d", new SecureRandom().nextLong() & Long.MAX_VALUE).substring(0, 16);
    }
}