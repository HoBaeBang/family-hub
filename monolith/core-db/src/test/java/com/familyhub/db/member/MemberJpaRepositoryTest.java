package com.familyhub.db.member;

import com.familyhub.domain.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@EntityScan("com.familyhub.domain")
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository repository;

    @Test
    void existsByEmail_returns_true_when_member_exists() {
        repository.save(Member.create("test@test.com", "pw", "홍길동"));
        assertThat(repository.existsByEmail("test@test.com")).isTrue();
    }

    @Test
    void existsByEmail_returns_false_when_not_exists() {
        assertThat(repository.existsByEmail("nobody@test.com")).isFalse();
    }

    @Test
    void findByEmail_returns_member_when_exists() {
        repository.save(Member.create("test@test.com", "pw", "홍길동"));
        assertThat(repository.findByEmail("test@test.com")).isPresent();
    }

    @Test
    void findByEmail_returns_empty_when_not_exists() {
        assertThat(repository.findByEmail("nobody@test.com")).isEmpty();
    }
}
