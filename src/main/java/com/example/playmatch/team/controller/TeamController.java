package com.example.playmatch.team.controller;

import com.example.playmatch.api.controller.TeamApi;
import com.example.playmatch.api.model.AddMembersRequest;
import com.example.playmatch.api.model.AddMembersByPhoneRequest;
import com.example.playmatch.api.model.BulkOperationResult;
import com.example.playmatch.api.model.ChangeRoleRequest;
import com.example.playmatch.api.model.CreateTeamRequest;
import com.example.playmatch.api.model.MemberSearchResponse;
import com.example.playmatch.api.model.TeamResponse;
import com.example.playmatch.api.model.TeamRole;
import com.example.playmatch.api.model.TeamSearchResponse;
import com.example.playmatch.api.model.UpdateTeamRequest;
import com.example.playmatch.api.model.UserTeamsResponse;
import com.example.playmatch.team.service.TeamService;
import com.example.playmatch.auth.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TeamController implements TeamApi {

  private final TeamService teamService;

  @Override
  public ResponseEntity<BulkOperationResult> _addTeamMembers(Long teamId, AddMembersRequest addMembersRequest) {
    log.info("Adding members to team: {}", teamId);
    BulkOperationResult result = teamService.addTeamMembers(teamId, addMembersRequest);
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<BulkOperationResult> _addTeamMembersByPhone(Long teamId, AddMembersByPhoneRequest addMembersByPhoneRequest) {
    log.info("Adding members to team {} by phone numbers: {}", teamId, addMembersByPhoneRequest.getPhoneNumbers());
    BulkOperationResult result = teamService.addTeamMembersByPhone(teamId, addMembersByPhoneRequest);
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<Void> _changeMemberRole(Long teamId, Long userId, ChangeRoleRequest changeRoleRequest) {
    log.info("Changing member role for user: {} in team: {}", userId, teamId);
    teamService.changeMemberRole(teamId, userId, changeRoleRequest);
    return ResponseEntity.noContent().build();
  }

  //TODO: remove validation for unique name
  @Override
  public ResponseEntity<TeamResponse> _createTeam(CreateTeamRequest createTeamRequest) {
    log.info("Creating new team: {}", createTeamRequest.getName());

    Long createdByUserId = getCurrentUserId();

    TeamResponse teamResponse = teamService.createTeam(createTeamRequest, createdByUserId);
    return ResponseEntity.status(HttpStatus.CREATED).body(teamResponse);
  }

  @Override
  public ResponseEntity<Void> _deleteTeam(Long teamId) {
    log.info("Deleting team: {}", teamId);
    teamService.deleteTeam(teamId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<TeamResponse> _getTeam(Long teamId) {
    log.info("Fetching team: {}", teamId);
    TeamResponse teamResponse = teamService.getTeam(teamId);
    return ResponseEntity.ok(teamResponse);
  }

  @Override
  public ResponseEntity<MemberSearchResponse> _listTeamMembers(Long teamId, TeamRole role, Integer limit, Integer offset) {
    log.info("Listing team members for team: {}", teamId);
    int actualLimit = limit != null ? limit : 20;
    int actualOffset = offset != null ? offset : 0;
    MemberSearchResponse response = teamService.listTeamMembers(teamId, role, actualLimit, actualOffset);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> _removeTeamMember(Long teamId, Long userId) {
    log.info("Removing member: {} from team: {}", userId, teamId);
    teamService.removeTeamMember(teamId, userId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<TeamSearchResponse> _searchTeams(String city, String name, Integer limit, Integer offset) {
    log.info("Searching teams with city: {}, name: {}", city, name);
    int actualLimit = limit != null ? limit : 20;
    int actualOffset = offset != null ? offset : 0;
    TeamSearchResponse response = teamService.searchTeams(city, name, actualLimit, actualOffset);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<TeamResponse> _updateTeam(Long teamId, UpdateTeamRequest updateTeamRequest) {
    log.info("Updating team: {}", teamId);
    TeamResponse teamResponse = teamService.updateTeam(teamId, updateTeamRequest);
    return ResponseEntity.ok(teamResponse);
  }

  @Override
  public ResponseEntity<UserTeamsResponse> _getUserTeams() {
    log.info("Fetching all teams for current user");
    Long userId = getCurrentUserId();
    UserTeamsResponse response = teamService.getUserTeams(userId);
    return ResponseEntity.ok(response);
  }

  private Long getCurrentUserId() {
    Long userId = CurrentUser.getUserId();
    if (userId == null) {
      throw new RuntimeException("Unable to determine current user (not authenticated)");
    }
    return userId;
  }
}
