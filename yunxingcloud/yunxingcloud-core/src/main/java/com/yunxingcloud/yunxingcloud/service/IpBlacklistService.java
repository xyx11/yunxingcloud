package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.IpBlacklist;
import com.yunxingcloud.yunxingcloud.repository.IpBlacklistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IpBlacklistService {

    private final IpBlacklistRepository repo;

    public IpBlacklistService(IpBlacklistRepository repo) {
        this.repo = repo;
    }

    public List<IpBlacklist> list() {
        return repo.findAll();
    }

    @Transactional
    public IpBlacklist add(IpBlacklist item) {
        if (repo.existsByIp(item.getIp())) {
            throw new IllegalArgumentException("ip_blacklist.exists");
        }
        return repo.save(item);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
