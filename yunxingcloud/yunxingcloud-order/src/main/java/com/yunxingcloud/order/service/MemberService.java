package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MemberService {

    private final PointsAccountRepository pointsRepo;
    private final MemberTierRepository tierRepo;

    public MemberService(PointsAccountRepository pointsRepo, MemberTierRepository tierRepo) {
        this.pointsRepo = pointsRepo; this.tierRepo = tierRepo;
    }

    /** 获取用户当前会员等级 */
    public MemberTier getTier(String username) {
        PointsAccount acc = pointsRepo.findByUsername(username).orElse(null);
        long totalEarned = acc != null ? acc.getTotalEarned() : 0;

        return tierRepo.findAllByOrderByMinPointsDesc().stream()
                .filter(t -> totalEarned >= t.getMinPoints())
                .findFirst().orElse(null);
    }

    /** 获取会员折扣 */
    public int getDiscountRate(String username) {
        MemberTier tier = getTier(username);
        return tier != null ? tier.getDiscountRate() : 100;
    }

    /** 会员权益概览 */
    public Map<String, Object> benefits(String username) {
        MemberTier tier = getTier(username);
        PointsAccount acc = pointsRepo.findByUsername(username).orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentTier", tier != null ? tier.getName() : "普通会员");
        result.put("totalPoints", acc != null ? acc.getTotalEarned() : 0);
        result.put("balance", acc != null ? acc.getBalance() : 0);

        // 下一级
        List<MemberTier> all = tierRepo.findAllByOrderByMinPointsAsc();
        MemberTier next = null;
        if (tier != null) {
            for (int i = 0; i < all.size() - 1; i++) {
                if (all.get(i).getId().equals(tier.getId())) {
                    next = all.get(i + 1);
                    break;
                }
            }
        } else if (!all.isEmpty()) {
            next = all.get(0);
        }
        if (next != null) {
            long need = next.getMinPoints() - (acc != null ? acc.getTotalEarned() : 0);
            result.put("nextTier", next.getName());
            result.put("needPoints", Math.max(0, need));
        }

        if (tier != null) {
            result.put("discountRate", tier.getDiscountRate());
            result.put("freeShipping", tier.getFreeShipping());
            result.put("birthdayGift", tier.getBirthdayGift());
        }

        return result;
    }
}