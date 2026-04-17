package com.familyhub.api.auth.oauth2;

import com.familyhub.db.member.MemberJpaRepository;
import com.familyhub.db.member.UserProviderJpaRepository;
import com.familyhub.domain.member.Member;
import com.familyhub.domain.member.ProviderType;
import com.familyhub.domain.member.UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService implements org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate;
    private final MemberJpaRepository memberRepository;
    private final UserProviderJpaRepository userProviderRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        ProviderType providerType = ProviderType.GOOGLE;

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(Member.createOAuth(email, name)));

        if (!userProviderRepository.existsByProviderTypeAndProviderId(providerType, providerId)) {
            userProviderRepository.save(UserProvider.create(member, providerType, providerId));
        }

        return new CustomOAuth2User(oAuth2User, member.getId());
    }
}
