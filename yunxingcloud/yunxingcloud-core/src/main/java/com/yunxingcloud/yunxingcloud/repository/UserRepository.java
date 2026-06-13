package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByUsernameContainingOrNicknameContainingOrEmailContaining(String kw1, String kw2, String kw3, Pageable pageable);
    List<User> findByApproved(boolean approved);
}
