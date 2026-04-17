package com.familyhub.api.auth.oauth2;

import com.familyhub.redis.auth.TempCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock TempCodeRepository tempCodeRepository;
    @Mock Authentication authentication;
    OAuth2SuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2SuccessHandler(tempCodeRepository, "http://localhost:3000/auth/callback");
    }

    @Test
    void onAuthenticationSuccess_saves_temp_code_and_redirects_to_frontend() throws Exception {
        CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
        when(oAuth2User.getMemberId()).thenReturn(1L);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(tempCodeRepository.save("1")).thenReturn("temp-code-uuid");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(tempCodeRepository).save("1");
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/auth/callback?code=temp-code-uuid");
    }
}
