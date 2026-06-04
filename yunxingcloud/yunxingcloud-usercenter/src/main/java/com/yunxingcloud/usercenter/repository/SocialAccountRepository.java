package com.yunxingcloud.usercenter.repository;

import com.yunxingcloud.usercenter.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    List<SocialAccount> findByUserId(Long userId);

    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}
