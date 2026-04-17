package com.familyhub.db.member;

import com.familyhub.domain.member.ProviderType;
import com.familyhub.domain.member.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProviderJpaRepository extends JpaRepository<UserProvider, Long> {
    boolean existsByProviderTypeAndProviderId(ProviderType providerType, String providerId);
}
