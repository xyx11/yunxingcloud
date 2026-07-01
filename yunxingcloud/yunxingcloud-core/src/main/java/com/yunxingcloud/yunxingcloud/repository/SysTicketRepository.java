package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysTicketRepository extends JpaRepository<SysTicket, Long> {
    List<SysTicket> findByApplicantOrderByCreatedAtDesc(String applicant);
    List<SysTicket> findByAssigneeOrderByCreatedAtDesc(String assignee);
    List<SysTicket> findByStatusOrderByCreatedAtDesc(String status);
}
