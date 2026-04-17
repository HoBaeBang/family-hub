package com.familyhub.db.member;

import com.familyhub.domain.member.Member;
import com.familyhub.domain.member.ProviderType;
import com.familyhub.domain.member.UserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@EntityScan("com.familyhub.domain")
class UserProviderJpaRepositoryTest {

    @Autowired UserProviderJpaRepository userProviderRepository;
    @Autowired MemberJpaRepository memberRepository;

    @Test
    void existsByProviderTypeAndProviderId_returns_true_when_exists() {
        Member member = memberRepository.save(Member.createOAuth("g@test.com", "구글유저"));
        userProviderRepository.save(UserProvider.create(member, ProviderType.GOOGLE, "google-sub-123"));

        assertThat(userProviderRepository.existsByProviderTypeAndProviderId(
                ProviderType.GOOGLE, "google-sub-123")).isTrue();
    }

    @Test
    void existsByProviderTypeAndProviderId_returns_false_when_not_exists() {
        assertThat(userProviderRepository.existsByProviderTypeAndProviderId(
                ProviderType.GOOGLE, "nobody")).isFalse();
    }
}
