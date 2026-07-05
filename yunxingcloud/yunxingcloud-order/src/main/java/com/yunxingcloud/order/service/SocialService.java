package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.ShareRecord;
import com.yunxingcloud.order.entity.Wishlist;
import com.yunxingcloud.order.repository.ShareRecordRepository;
import com.yunxingcloud.order.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SocialService {

    private final WishlistRepository wishRepo;
    private final ShareRecordRepository shareRepo;

    public SocialService(WishlistRepository wishRepo, ShareRecordRepository shareRepo) {
        this.wishRepo = wishRepo; this.shareRepo = shareRepo;
    }

    public List<Wishlist> wishlist(String username) { return wishRepo.findByUsername(username); }

    @Transactional
    public Wishlist addWish(String username, Long productId) {
        if (wishRepo.existsByUsernameAndProductId(username, productId)) return null;
        Wishlist w = new Wishlist(); w.setUsername(username); w.setProductId(productId);
        return wishRepo.save(w);
    }

    @Transactional
    public void removeWish(String username, Long productId) {
        wishRepo.deleteByUsernameAndProductId(username, productId);
    }

    @Transactional
    public ShareRecord share(String sharer, Long productId, String channel) {
        ShareRecord sr = new ShareRecord();
        sr.setSharer(sharer); sr.setProductId(productId);
        sr.setChannel(channel != null ? channel : "copy");
        return shareRepo.save(sr);
    }

    @Transactional
    public void click(Long id) {
        shareRepo.findById(id).ifPresent(s -> { s.setClickCount(s.getClickCount() + 1); shareRepo.save(s); });
    }
}
