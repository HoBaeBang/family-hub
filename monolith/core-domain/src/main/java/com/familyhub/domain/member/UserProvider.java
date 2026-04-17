package com.familyhub.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_providers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider_type", "provider_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 20)
    private ProviderType providerType;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserProvider create(Member member, ProviderType providerType, String providerId) {
        UserProvider up = new UserProvider();
        up.member = member;
        up.providerType = providerType;
        up.providerId = providerId;
        up.createdAt = LocalDateTime.now();
        return up;
    }
}
