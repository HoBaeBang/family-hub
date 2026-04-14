// monolith/core-domain/src/test/java/com/familyhub/domain/member/MemberTest.java
package com.familyhub.domain.member;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MemberTest {

    @Test
    void create_sets_all_fields_and_default_role() {
        Member member = Member.create("test@test.com", "encoded_pw", "홍길동");

        assertThat(member.getEmail()).isEqualTo("test@test.com");
        assertThat(member.getPassword()).isEqualTo("encoded_pw");
        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getRole()).isEqualTo(MemberRole.USER);
        assertThat(member.getCreatedAt()).isNotNull();
    }
}
