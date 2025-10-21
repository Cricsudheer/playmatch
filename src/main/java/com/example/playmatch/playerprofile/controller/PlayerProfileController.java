package com.example.playmatch.playerprofile.controller;

import com.example.playmatch.api.controller.PlayerProfileApi;
import com.example.playmatch.api.model.CreatePlayerProfileRequest;
import com.example.playmatch.api.model.PlayerProfileResponse;
import com.example.playmatch.api.model.PrimaryRole;
import com.example.playmatch.api.model.SearchResponse;
import com.example.playmatch.api.model.UpdatePlayerProfileRequest;
import com.example.playmatch.auth.security.RequireAuthentication;
import com.example.playmatch.auth.security.CurrentUser;
import com.example.playmatch.playerprofile.service.PlayerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class PlayerProfileController implements PlayerProfileApi {

  private final PlayerProfileService playerProfileService;

  @RequireAuthentication
  @Override
  public ResponseEntity<PlayerProfileResponse> _createPlayerProfile(CreatePlayerProfileRequest createPlayerProfileRequest) {
    Long userId = CurrentUser.getUserId();
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    log.info("Creating player profile for userId={}", userId);
    try {
      PlayerProfileResponse response = playerProfileService.createPlayerProfile(userId, createPlayerProfileRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalStateException conflict) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (IllegalArgumentException badRequest) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @RequireAuthentication
  @Override
  public ResponseEntity<PlayerProfileResponse> _getPlayerProfileByUserId(Long userId) {
    Long currentUserId = CurrentUser.getUserId();
    if (currentUserId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (!currentUserId.equals(userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      PlayerProfileResponse response = playerProfileService.getPlayerProfileByUserId(userId);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException notFound) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @RequireAuthentication
  @Override
  public ResponseEntity<SearchResponse> _searchPlayers(String city, PrimaryRole primaryRole, Integer limit, Integer offset) {
    if (CurrentUser.getUserId() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    int safeLimit = (limit == null) ? 20 : Math.min(Math.max(limit, 1), 100);
    int safeOffset = (offset == null) ? 0 : Math.max(offset, 0);
    SearchResponse response = playerProfileService.searchPlayers(city, primaryRole, safeLimit, safeOffset);
    return ResponseEntity.ok(response);
  }

  @RequireAuthentication
  @Override
  public ResponseEntity<PlayerProfileResponse> _updatePlayerProfile(UpdatePlayerProfileRequest updatePlayerProfileRequest) {
    Long userId = CurrentUser.getUserId();
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    try {
      PlayerProfileResponse response = playerProfileService.updatePlayerProfile(userId, updatePlayerProfileRequest);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException badRequest) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (IllegalStateException conflict) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }
}
