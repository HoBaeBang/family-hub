package com.familyhub.api.auth;

import com.familyhub.api.auth.dto.*;
import com.familyhub.api.exception.AppException;
import com.familyhub.api.exception.ErrorCode;
import com.familyhub.api.security.JwtProvider;
import com.familyhub.db.member.MemberJpaRepository;
import com.familyhub.domain.member.Member;
import com.familyhub.redis.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final MemberJpaRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        Member saved = memberRepository.save(
                Member.create(request.email(), passwordEncoder.encode(request.password()), request.name())
        );
        return new SignupResponse(saved.getId(), saved.getEmail());
    }

    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.generateAccessToken(member.getId(), member.getEmail());
        String tokenId = UUID.randomUUID().toString();
        refreshTokenRepository.save(tokenId, String.valueOf(member.getId()));

        return new TokenResponse(accessToken, tokenId, 900);
    }

    public TokenResponse refresh(RefreshRequest request) {
        String memberId = refreshTokenRepository.findMemberIdByTokenId(request.refreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        refreshTokenRepository.delete(request.refreshToken());

        Member member = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        String accessToken = jwtProvider.generateAccessToken(member.getId(), member.getEmail());
        String newTokenId = UUID.randomUUID().toString();
        refreshTokenRepository.save(newTokenId, memberId);

        return new TokenResponse(accessToken, newTokenId, 900);
    }

    public void logout(LogoutRequest request) {
        refreshTokenRepository.delete(request.refreshToken());
    }
}
