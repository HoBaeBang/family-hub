package com.familyhub.api.auth.oauth2;

import com.familyhub.db.member.MemberJpaRepository;
import com.familyhub.db.member.UserProviderJpaRepository;
import com.familyhub.domain.member.Member;
import com.familyhub.domain.member.ProviderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock MemberJpaRepository memberRepository;
    @Mock UserProviderJpaRepository userProviderRepository;
    @Mock DefaultOAuth2UserService delegate;
    @InjectMocks
    CustomOAuth2UserService oAuth2UserService;

    private OAuth2UserRequest buildRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/callback")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .build();

        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "token", Instant.now(), Instant.now().plusSeconds(3600));

        return new OAuth2UserRequest(registration, token);
    }

    private OAuth2User buildGoogleUser() {
        return new DefaultOAuth2User(
                List.of(),
                Map.of("sub", "google-sub-123", "email", "g@test.com", "name", "구글유저"),
                "sub"
        );
    }

    @Test
    void loadUser_creates_new_member_and_provider_when_first_login() {
        OAuth2UserRequest request = buildRequest();
        when(delegate.loadUser(request)).thenReturn(buildGoogleUser());
        when(memberRepository.findByEmail("g@test.com")).thenReturn(Optional.empty());
        Member saved = Member.createOAuth("g@test.com", "구글유저");
        when(memberRepository.save(any())).thenReturn(saved);
        when(userProviderRepository.existsByProviderTypeAndProviderId(ProviderType.GOOGLE, "google-sub-123"))
                .thenReturn(false);

        OAuth2User result = oAuth2UserService.loadUser(request);

        assertThat(result).isInstanceOf(CustomOAuth2User.class);
        verify(memberRepository).save(any());
        verify(userProviderRepository).save(any());
    }

    @Test
    void loadUser_reuses_existing_member_and_skips_provider_when_already_exists() {
        OAuth2UserRequest request = buildRequest();
        when(delegate.loadUser(request)).thenReturn(buildGoogleUser());
        Member existing = Member.createOAuth("g@test.com", "구글유저");
        when(memberRepository.findByEmail("g@test.com")).thenReturn(Optional.of(existing));
        when(userProviderRepository.existsByProviderTypeAndProviderId(ProviderType.GOOGLE, "google-sub-123"))
                .thenReturn(true);

        oAuth2UserService.loadUser(request);

        verify(memberRepository, never()).save(any());
        verify(userProviderRepository, never()).save(any());
    }
}
