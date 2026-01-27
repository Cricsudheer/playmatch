package com.example.playmatch.mvp.invites.service;

import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import com.example.playmatch.mvp.invites.repository.MatchInviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InviteTokenGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 8;
    private static final int MAX_RETRIES = 3;

    private final MatchInviteRepository matchInviteRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a unique 8-character uppercase alphanumeric invite token
     * Retries up to MAX_RETRIES times if collision occurs
     *
     * @return Unique invite token
     * @throws MvpException if unable to generate unique token after retries
     */
    public String generate() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String token = secureRandom.ints(TOKEN_LENGTH, 0, CHARS.length())
                .mapToObj(CHARS::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());

            // Check uniqueness
            if (!matchInviteRepository.existsByInviteToken(token)) {
                log.debug("Generated unique invite token: {} (attempt {})", token, attempt + 1);
                return token;
            }

            log.warn("Invite token collision detected: {} (attempt {})", token, attempt + 1);
        }

        throw new MvpException(MvpError.INVITE_GENERATION_FAILED,
            "Failed to generate unique invite token after " + MAX_RETRIES + " attempts");
    }
}
