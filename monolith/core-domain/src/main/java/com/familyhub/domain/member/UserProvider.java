package com.familyhub.domain.member;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_providers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider_type", "provider_id"}))
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
        UserProvider userProvider = new UserProvider();
        userProvider.member = member;
        userProvider.providerType = providerType;
        userProvider.providerId = providerId;
        userProvider.createdAt = LocalDateTime.now();
        return userProvider;
    }
}
