package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUsernameOrderByCreatedAtDesc(String username);
}