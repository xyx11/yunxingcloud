package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.PointsAccount;
import com.yunxingcloud.order.entity.PointsRecord;
import com.yunxingcloud.order.repository.PointsAccountRepository;
import com.yunxingcloud.order.repository.PointsRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsService {

    private final PointsAccountRepository accountRepo;
    private final PointsRecordRepository recordRepo;

    public PointsService(PointsAccountRepository accountRepo, PointsRecordRepository recordRepo) {
        this.accountRepo = accountRepo;
        this.recordRepo = recordRepo;
    }

    /** 注册送积分 */
    @Transactional
    public void earnOnRegister(String username) {
        earn(username, 100L, "register", null, "注册赠送");
    }

    /** 购物返积分 (消费金额的 1%) */
    @Transactional
    public void earnOnPurchase(String username, Long orderId, Long amountInCents) {
        long points = Math.max(1, amountInCents / 100);
        earn(username, points, "purchase", orderId, "购物返积分");
    }

    /** 积分抵扣 (100分=1元) */
    @Transactional
    public long redeem(String username, Long points, Long orderId) {
        PointsAccount account = getOrCreate(username);
        if (account.getBalance() < points) throw new IllegalStateException("积分不足");
        spend(username, points, "redeem", orderId, "积分抵扣");
        return points / 100; // 返回抵扣金额(分)
    }

    private void earn(String username, Long amount, String type, Long orderId, String remark) {
        PointsAccount account = getOrCreate(username);
        account.setBalance(account.getBalance() + amount);
        account.setTotalEarned(account.getTotalEarned() + amount);
        accountRepo.save(account);
        record(username, amount, type, orderId, remark, account.getBalance());
    }

    private void spend(String username, Long amount, String type, Long orderId, String remark) {
        PointsAccount account = getOrCreate(username);
        account.setBalance(account.getBalance() - amount);
        account.setTotalSpent(account.getTotalSpent() + amount);
        accountRepo.save(account);
        record(username, -amount, type, orderId, remark, account.getBalance());
    }

    private PointsAccount getOrCreate(String username) {
        return accountRepo.findByUsername(username).orElseGet(() -> {
            PointsAccount a = new PointsAccount();
            a.setUsername(username);
            a.setBalance(0L);
            return accountRepo.save(a);
        });
    }

    private void record(String username, Long amount, String type, Long orderId, String remark, Long balanceAfter) {
        PointsRecord r = new PointsRecord();
        r.setUsername(username); r.setAmount(amount); r.setType(type);
        r.setOrderId(orderId); r.setRemark(remark); r.setBalanceAfter(balanceAfter);
        recordRepo.save(r);
    }

    public PointsAccount getAccount(String username) {
        return accountRepo.findByUsername(username).orElse(null);
    }
}