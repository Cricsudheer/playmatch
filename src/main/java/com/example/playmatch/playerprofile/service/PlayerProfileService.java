package com.example.playmatch.playerprofile.service;

import com.example.playmatch.api.model.*;
import com.example.playmatch.auth.model.User;
import com.example.playmatch.auth.repository.UserRepository;
import com.example.playmatch.playerprofile.model.PlayerProfile;
import com.example.playmatch.playerprofile.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final UserRepository userRepository;

    public PlayerProfileResponse createPlayerProfile(UUID userId, CreatePlayerProfileRequest request) {
        log.info("Creating player profile for user: {}", userId);

        // Check if user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check if profile already exists
        if (playerProfileRepository.existsByUser_Id(userId)) {
            throw new IllegalStateException("Player profile already exists for user: " + userId);
        }

        // Check if mobile number is already registered
        if (playerProfileRepository.existsByMobile(request.getMobile())) {
            throw new IllegalStateException("Mobile number already registered: " + request.getMobile());
        }

        // Validate code of conduct acceptance
        if (!Boolean.TRUE.equals(request.getCodeOfConductAccepted())) {
            throw new IllegalArgumentException("Code of conduct must be accepted");
        }

        // Create player profile
        PlayerProfile playerProfile = PlayerProfile.builder()
            .user(user)
            .fullName(request.getFullName())
            .gender(request.getGender())
            .mobile(request.getMobile())
            .city(request.getCity())
            .primaryRole(request.getPrimaryRole())
            .jerseySize(request.getJerseySize())
            .upiId(request.getUpiId())
            .codeOfConductAccepted(request.getCodeOfConductAccepted())
            .profilePhotoUrl(request.getProfilePhotoUrl() != null ? request.getProfilePhotoUrl().toString() : null)
            .build();

        PlayerProfile savedProfile = playerProfileRepository.save(playerProfile);
        log.info("Player profile created successfully with ID: {}", savedProfile.getId());

        return mapToResponse(savedProfile);
    }

    public PlayerProfileResponse updatePlayerProfile(UUID userId, UpdatePlayerProfileRequest request) {
        log.info("Updating player profile for user: {}", userId);

        PlayerProfile playerProfile = playerProfileRepository.findByUser_Id(userId)
            .orElseThrow(() -> new IllegalArgumentException("Player profile not found for user: " + userId));

        // Check mobile number uniqueness if being updated
        if (request.getMobile() != null && !request.getMobile().equals(playerProfile.getMobile())) {
            if (playerProfileRepository.existsByMobile(request.getMobile())) {
                throw new IllegalStateException("Mobile number already registered: " + request.getMobile());
            }
        }

        // Update only provided fields
        if (request.getFullName() != null) {
            playerProfile.setFullName(request.getFullName());
        }
        if (request.getGender() != null) {
            playerProfile.setGender(request.getGender());
        }
        if (request.getMobile() != null) {
            playerProfile.setMobile(request.getMobile());
        }
        if (request.getCity() != null) {
            playerProfile.setCity(request.getCity());
        }
        if (request.getPrimaryRole() != null) {
            playerProfile.setPrimaryRole(request.getPrimaryRole());
        }
        if (request.getJerseySize() != null) {
            playerProfile.setJerseySize(request.getJerseySize());
        }
        if (request.getUpiId() != null) {
            playerProfile.setUpiId(request.getUpiId());
        }
        if (request.getCodeOfConductAccepted() != null) {
            playerProfile.setCodeOfConductAccepted(request.getCodeOfConductAccepted());
        }
        if (request.getProfilePhotoUrl() != null) {
            playerProfile.setProfilePhotoUrl(request.getProfilePhotoUrl().toString());
        }

        PlayerProfile updatedProfile = playerProfileRepository.save(playerProfile);
        log.info("Player profile updated successfully for user: {}", userId);

        return mapToResponse(updatedProfile);
    }

    @Transactional(readOnly = true)
    public PlayerProfileResponse getPlayerProfileByUserId(UUID userId) {
        log.info("Fetching player profile for user: {}", userId);

        PlayerProfile playerProfile = playerProfileRepository.findByUser_Id(userId)
            .orElseThrow(() -> new IllegalArgumentException("Player profile not found for user: " + userId));

        return mapToResponse(playerProfile);
    }

    @Transactional(readOnly = true)
    public SearchResponse searchPlayers(String city, PrimaryRole primaryRole, int limit, int offset) {
        log.info("Searching players with city: {}, primaryRole: {}, limit: {}, offset: {}",
                city, primaryRole, limit, offset);

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<PlayerProfile> playerPage = playerProfileRepository.findByFilters(city, primaryRole, pageable);

        List<PlayerSummary> playerSummaries = playerPage.getContent().stream()
            .map(this::mapToSummary)
            .toList();

        return new SearchResponse()
            .total(playerPage.getTotalElements())
            .items(playerSummaries);
    }

    private PlayerProfileResponse mapToResponse(PlayerProfile playerProfile) {
        return new PlayerProfileResponse()
            .id(playerProfile.getId())
            .userId(playerProfile.getUser().getId())
            .fullName(playerProfile.getFullName())
            .gender(playerProfile.getGender())
            .mobile(playerProfile.getMobile())
            .city(playerProfile.getCity())
            .primaryRole(playerProfile.getPrimaryRole())
            .jerseySize(playerProfile.getJerseySize())
            .upiId(playerProfile.getUpiId())
            .codeOfConductAccepted(playerProfile.getCodeOfConductAccepted())
            .profilePhotoUrl(playerProfile.getProfilePhotoUrl() != null ?
                java.net.URI.create(playerProfile.getProfilePhotoUrl()) : null)
            .createdAt(playerProfile.getCreatedAt())
            .updatedAt(playerProfile.getUpdatedAt());
    }

    /**
     * Converts a Long ID to a deterministic UUID
     * This ensures the same Long ID always produces the same UUID
     */
    private UUID convertLongToUUID(Long id) {
        if (id == null) {
            return null;
        }
        // Create a deterministic UUID from the Long ID
        // Using a fixed namespace UUID to ensure consistency
        String namespace = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"; // Fixed namespace UUID
        String idString = "player_profile_" + id;
        return UUID.nameUUIDFromBytes((namespace + idString).getBytes());
    }

    private PlayerSummary mapToSummary(PlayerProfile playerProfile) {
        return new PlayerSummary()
            .userId(playerProfile.getUser().getId())
            .fullName(playerProfile.getFullName())
            .city(playerProfile.getCity())
            .primaryRole(playerProfile.getPrimaryRole())
            .jerseySize(playerProfile.getJerseySize())
            .profilePhotoUrl(playerProfile.getProfilePhotoUrl() != null ?
                java.net.URI.create(playerProfile.getProfilePhotoUrl()) : null);
    }
}
