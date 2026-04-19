package com.familyhub.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Member create(String email, String password, String name) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.name = name;
        member.role = MemberRole.USER;
        member.createdAt = LocalDateTime.now();
        return member;
    }

    public static Member createOAuth(String email, String name) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.role = MemberRole.USER;
        member.createdAt = LocalDateTime.now();
        return member;
    }

    public static Member createOAuth(String email, String name) {
        Member m = new Member();
        m.email = email;
        m.password = null;
        m.name = name;
        m.role = MemberRole.USER;
        m.createdAt = LocalDateTime.now();
        return m;
    }
}
