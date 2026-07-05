package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Merchant;
import com.yunxingcloud.order.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepo;

    public MerchantService(MerchantRepository merchantRepo) { this.merchantRepo = merchantRepo; }

    @Transactional
    public Merchant apply(Merchant merchant) {
        if (merchantRepo.findByPhone(merchant.getPhone()) != null)
            throw new IllegalArgumentException("该手机号已提交过申请");
        merchant.setStatus("0");
        return merchantRepo.save(merchant);
    }

    public Optional<Merchant> status(String phone) {
        return Optional.ofNullable(merchantRepo.findByPhone(phone));
    }

    public List<Merchant> list(String status) {
        return merchantRepo.findByStatus(status);
    }

    @Transactional
    public Merchant approve(Long id) {
        Merchant m = merchantRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));
        if (!"0".equals(m.getStatus()))
            throw new IllegalStateException("当前状态不可审核");
        m.setStatus("1");
        return merchantRepo.save(m);
    }

    @Transactional
    public Merchant reject(Long id) {
        Merchant m = merchantRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));
        if (!"0".equals(m.getStatus()))
            throw new IllegalStateException("当前状态不可审核");
        m.setStatus("2");
        return merchantRepo.save(m);
    }
}
