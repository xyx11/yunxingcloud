package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.Approval;
import com.yunxingcloud.yunxingcloud.repository.ApprovalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ApprovalService {

    private final ApprovalRepository repo;

    public ApprovalService(ApprovalRepository repo) {
        this.repo = repo;
    }

    public List<Approval> list(String username) {
        return repo.findByApplicantOrderByCreatedAtDesc(username);
    }

    public List<Approval> pending(String username) {
        return repo.findByApproverOrderByCreatedAtDesc(username);
    }

    @Transactional
    public Approval create(Approval approval) {
        return repo.save(approval);
    }

    @Transactional
    public Optional<Approval> approve(Long id, String remark) {
        return repo.findById(id).map(a -> {
            a.setStatus("1");
            a.setRemark(remark != null ? remark : "已通过");
            return repo.save(a);
        });
    }

    @Transactional
    public Optional<Approval> reject(Long id, String remark) {
        return repo.findById(id).map(a -> {
            a.setStatus("2");
            a.setRemark(remark != null ? remark : "已驳回");
            return repo.save(a);
        });
    }
}
