package com.familyhub.db.member;

import com.familyhub.domain.member.Member;
import com.familyhub.domain.member.ProviderType;
import com.familyhub.domain.member.UserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@EntityScan("com.familyhub.domain")
public class UserProviderJpaRepositoryTest {

    @Autowired
    UserProviderJpaRepository userProviderJpaRepository;
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void existsByProviderTypeAndProviderId_return_true_when_exists() {
        Member member = memberJpaRepository.save(Member.createOAuth("g@test.com", "구글 유저"));
        userProviderJpaRepository.save(UserProvider.create(member, ProviderType.GOOGLE, "google-sub-123"));

        assertThat(userProviderJpaRepository.existsByProviderTypeAndProviderId(
                ProviderType.GOOGLE, "google-sub-123"
        )).isTrue();
    }
    @Test
    void existsByProviderTypeAndProviderId_returns_false_when_not_exists() {
        assertThat(userProviderJpaRepository.existsByProviderTypeAndProviderId(
                ProviderType.GOOGLE, "nobody")).isFalse();
    }
}
