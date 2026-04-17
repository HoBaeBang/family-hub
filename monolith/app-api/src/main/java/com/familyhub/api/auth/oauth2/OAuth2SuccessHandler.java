package com.familyhub.api.auth.oauth2;

import com.familyhub.redis.auth.TempCodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TempCodeRepository tempCodeRepository;
    private final String redirectUri;

    public OAuth2SuccessHandler(TempCodeRepository tempCodeRepository, String redirectUri) {
        this.tempCodeRepository = tempCodeRepository;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String code = tempCodeRepository.save(String.valueOf(oAuth2User.getMemberId()));
        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?code=" + code);
    }
}
