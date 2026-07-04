package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.LiveRoom;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LiveRoomRepository extends JpaRepository<LiveRoom, Long> {
    List<LiveRoom> findByStatus(String status, Sort sort);
}
