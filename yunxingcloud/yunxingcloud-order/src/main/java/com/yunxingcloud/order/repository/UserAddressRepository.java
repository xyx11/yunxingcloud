package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUsernameOrderByIsDefaultDesc(String username);
}
