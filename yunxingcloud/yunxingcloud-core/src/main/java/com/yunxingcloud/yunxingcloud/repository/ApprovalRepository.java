package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByApplicantOrderByCreatedAtDesc(String applicant);
    List<Approval> findByApproverOrderByCreatedAtDesc(String approver);
    List<Approval> findByStatusOrderByCreatedAtDesc(String status);
}
