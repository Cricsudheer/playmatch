package com.example.playmatch.team.service;

import com.example.playmatch.api.model.*;
import com.example.playmatch.auth.repository.UserRepository;
import com.example.playmatch.playerprofile.repository.PlayerProfileRepository;
import com.example.playmatch.team.exception.TeamError;
import com.example.playmatch.team.exception.TeamException;
import com.example.playmatch.team.model.Team;
import com.example.playmatch.team.model.TeamMember;
import com.example.playmatch.team.model.enums.TeamRole;
import com.example.playmatch.team.repository.TeamRepository;
import com.example.playmatch.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public TeamResponse createTeam(CreateTeamRequest request, Long createdByUserId) {
        log.info("Creating team with name: {}", request.getName());

        Team team = Team.builder()
                .name(request.getName())
                .city(request.getCity())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl() != null ? request.getLogoUrl().toString() : null)
                .createdByUserId(createdByUserId)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Add creator as admin
        var creatorUser = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + createdByUserId));

        TeamMember creatorMember = TeamMember.builder()
                .team(savedTeam)
                .user(creatorUser)
                .role(TeamRole.ADMIN)
                .build();
        teamMemberRepository.save(creatorMember);

        return convertToTeamResponse(savedTeam);
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeam(Long teamId) {
        log.info("Fetching team with id: {}", teamId);
        Team team = teamRepository.findByIdAndIsActiveTrue(teamId)
                .orElseThrow(() -> new TeamException(TeamError.TEAM_NOT_FOUND , "Team not found with id: " + teamId));
        return convertToTeamResponse(team);
    }

    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request) {
        log.info("Updating team with id: {}", teamId);
        Team team = teamRepository.findByIdAndIsActiveTrue(teamId)
                .orElseThrow(() ->new TeamException(TeamError.TEAM_NOT_FOUND , "Team not found with id: " + teamId));

        if (request.getName() != null) {
            team.setName(request.getName());
        }

        if (request.getCity() != null) {
            team.setCity(request.getCity());
        }

        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }

        if (request.getLogoUrl() != null) {
            team.setLogoUrl(request.getLogoUrl().toString());
        }

        Team updatedTeam = teamRepository.save(team);
        return convertToTeamResponse(updatedTeam);
    }

    public void deleteTeam(Long teamId) {
        log.info("Deleting team with id: {}", teamId);
        Team team = teamRepository.findByIdAndIsActiveTrue(teamId)
                .orElseThrow(() -> new TeamException(TeamError.TEAM_NOT_FOUND , "Team not found with id: " + teamId));

        // Delete all team members before soft-deleting the team
        teamMemberRepository.deleteByTeamId(teamId);
        log.info("Deleted all team members for team {}", teamId);

        team.setIsActive(false);
        teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public TeamSearchResponse searchTeams(String city, String name, Integer limit, Integer offset) {
        log.info("Searching teams with city: {}, name: {}", city, name);

        // Validate pagination parameters to prevent division by zero
        if (limit == null || limit <= 0) {
            limit = 20; // Default limit
        }
        if (offset == null || offset < 0) {
            offset = 0; // Default offset
        }

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Team> teamsPage = teamRepository.findTeamsByCriteria(city, name, pageable);

        List<TeamSearchResponseItemsInner> teams = teamsPage.getContent().stream()
                .map(this::convertToTeamSearchItem)
                .collect(Collectors.toList());

        TeamSearchResponse response = new TeamSearchResponse();
        response.setTotal(teamsPage.getTotalElements());
        response.setItems(teams);
        return response;
    }


    //TODO : OPTIMISE THIS METHOD TO REDUCE THE NUMBER OF QUERIES
    public BulkOperationResult addTeamMembers(Long teamId, AddMembersRequest request) {
        log.info("Adding members to team: {}", teamId);

        Team team = teamRepository.findByIdAndIsActiveTrue(teamId)
                .orElseThrow(() -> new TeamException(TeamError.TEAM_NOT_FOUND , "Team not found with id: " + teamId));

        List<Long> successIds = new ArrayList<>();
        List<BulkOperationResultFailedInner> failed = new ArrayList<>();

        for (Long userId : request.getPlayerIds()) {
            try {
                // Fetch the user
                var user = userRepository.findById(userId);
                if (user.isEmpty()) {
                    BulkOperationResultFailedInner failedItem = new BulkOperationResultFailedInner();
                    failedItem.setUserId(java.util.UUID.fromString(userId.toString()));
                    failedItem.setReason("User not found");
                    failed.add(failedItem);
                    continue;
                }

                // Check if already a member
                if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
                    BulkOperationResultFailedInner failedItem = new BulkOperationResultFailedInner();
                    failedItem.setUserId(java.util.UUID.fromString(userId.toString()));
                    failedItem.setReason("User is already a team member");
                    failed.add(failedItem);
                    continue;
                }

                TeamMember member = TeamMember.builder()
                        .team(team)
                        .user(user.get())
                        .role(TeamRole.PLAYER)
                        .build();
                teamMemberRepository.save(member);

                successIds.add(userId);

            } catch (Exception e) {
                log.error("Error adding user {} to team {}: {}", userId, teamId, e.getMessage());
                BulkOperationResultFailedInner failedItem = new BulkOperationResultFailedInner();
                failedItem.setUserId(java.util.UUID.fromString(userId.toString()));
                failedItem.setReason(e.getMessage());
                failed.add(failedItem);
            }
        }

        BulkOperationResult result = new BulkOperationResult();
        result.setSuccessIds(successIds);
        result.setFailed(failed);
        return result;
    }

    public BulkOperationResult addTeamMembersByPhone(Long teamId, AddMembersByPhoneRequest request) {
        log.info("Adding members to team {} by phone numbers", teamId);

        // Validate team exists
        Team team = teamRepository.findByIdAndIsActiveTrue(teamId)
                .orElseThrow(() -> new TeamException(TeamError.TEAM_NOT_FOUND, "Team not found with id: " + teamId));

        List<Long> successIds = new ArrayList<>();
        List<BulkOperationResultFailedInner> failed = new ArrayList<>();

        // Default role is PLAYER if not specified
        TeamRole memberRole = request.getRole() != null
            ? TeamRole.valueOf(request.getRole().name())
            : TeamRole.PLAYER;

        for (String phoneNumber : request.getPhoneNumbers()) {
            try {
                // Validate phone number format
                if (!phoneNumber.matches("^[0-9]{10}$")) {
                    BulkOperationResultFailedInner failedItem = new BulkOperationResultFailedInner();
                    failedItem.setUserId(null);
                    failedItem.setReason("Invalid phone number format: " + phoneNumber + ". Must be 10 digits.");
                    failed.add(failedItem);
                    continue;
                }

                // Find player profile by mobile number
                var playerProfileOpt = playerProfileRepository.findByMobile(phoneNumber);
                if (playerProfileOpt.isEmpty()) {
                    BulkOperationResultFailedInner failedItem = new BulkOperationResultFailedInner();
                    failedItem.setUserId(null);
                    failedItem.setReason("No player profile found with phone number: " + phoneNumber);
                    failed.add(failedItem);
                    continue;
                }

                var playerProfile = playerProfileOpt.get();
                Long userId = playerProfile.getUser().getId();

                // Check if already a member
                if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
                    BulkOperationResultFailedInner failedItem = new BulkOperationResultFailedInner();
                    failedItem.setUserId(java.util.UUID.fromString(userId.toString()));
                    failedItem.setReason("User with phone " + phoneNumber + " is already a team member");
                    failed.add(failedItem);
                    continue;
                }

                // Add member to team
                TeamMember member = TeamMember.builder()
                        .team(team)
                        .user(playerProfile.getUser())
                        .role(memberRole)
                        .build();
                teamMemberRepository.save(member);

                successIds.add(userId);
                log.info("Successfully added user {} (phone: {}) to team {}", userId, phoneNumber, teamId);

            } catch (Exception e) {
                log.error("Error adding user with phone {} to team {}: {}", phoneNumber, teamId, e.getMessage());
                BulkOperationResultFailedInner failedItem = new BulkOperationResultFailedInner();
                failedItem.setUserId(null);
                failedItem.setReason("Error processing phone " + phoneNumber + ": " + e.getMessage());
                failed.add(failedItem);
            }
        }

        BulkOperationResult result = new BulkOperationResult();
        result.setSuccessIds(successIds);
        result.setFailed(failed);
        return result;
    }

    public void removeTeamMember(Long teamId, Long userId) {
        log.info("Removing member {} from team: {}", userId, teamId);

        // Check if team exists and is active
        if (!teamRepository.findByIdAndIsActiveTrue(teamId).isPresent()) {
            throw new TeamException(TeamError.TEAM_NOT_FOUND , "Team not found with id: " + teamId);
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() ->new TeamException(TeamError.MEMBER_NOT_FOUND, "Member not found in team"));

        teamMemberRepository.delete(member);
    }

    public void changeMemberRole(Long teamId, Long userId, ChangeRoleRequest request) {
        log.info("Changing role for member {} in team: {}", userId, teamId);

        // Check if team exists and is active
        if (!teamRepository.findByIdAndIsActiveTrue(teamId).isPresent()) {
            throw new TeamException(TeamError.TEAM_NOT_FOUND , "Team not found with id: " + teamId);
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new TeamException(TeamError.MEMBER_NOT_FOUND, "Member not found in team"));

        TeamRole newRole = TeamRole.valueOf(request.getRole().name());
        member.setRole(newRole);
        teamMemberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberSearchResponse listTeamMembers(Long teamId, com.example.playmatch.api.model.TeamRole role, Integer limit, Integer offset) {
        log.info("Listing members for team: {}", teamId);

        if (!teamRepository.existsById(teamId)) {
            throw new  TeamException(TeamError.TEAM_NOT_FOUND , "Team not found with id: " + teamId);
        }

        Pageable pageable = PageRequest.of(offset / limit, limit);
        TeamRole enumRole = role != null ? TeamRole.valueOf(role.name()) : null;

        Page<TeamMember> membersPage = teamMemberRepository.findByTeamIdAndRole(teamId, enumRole, pageable);

        List<com.example.playmatch.api.model.TeamMember> members = membersPage.getContent().stream()
                .map(this::convertToApiTeamMember)
                .collect(Collectors.toList());

        MemberSearchResponse response = new MemberSearchResponse();
        response.setTotal(membersPage.getTotalElements());
        response.setItems(members);
        return response;
    }

    @Transactional(readOnly = true)
    public UserTeamsResponse getUserTeams(Long userId) {
        log.info("Fetching all teams for user: {}", userId);

        List<TeamMember> userTeamMemberships = teamMemberRepository.findByUserId(userId);

        List<UserTeamSummary> teams = userTeamMemberships.stream()
                .map(tm -> {
                    UserTeamSummary summary = new UserTeamSummary();
                    summary.setTeamId(tm.getTeam().getId());
                    summary.setTeamName(tm.getTeam().getName());
                    summary.setRole(com.example.playmatch.api.model.TeamRole.valueOf(tm.getRole().name()));

                    // Get player count for this team
                    Long playerCount = teamMemberRepository.countByTeamId(tm.getTeam().getId());
                    summary.setPlayerCount(playerCount);

                    return summary;
                })
                .collect(Collectors.toList());

        UserTeamsResponse response = new UserTeamsResponse();
        response.setTeams(teams);
        return response;
    }

    private TeamResponse convertToTeamResponse(Team team) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setName(team.getName());
        response.setCity(team.getCity());
        response.setDescription(team.getDescription());
        if (team.getLogoUrl() != null) {
            try {
                response.setLogoUrl(java.net.URI.create(team.getLogoUrl()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid logo URL for team {}: {}", team.getId(), team.getLogoUrl());
            }
        }
        response.setIsActive(team.getIsActive());
        response.setCreatedByUserId(team.getCreatedByUserId());
        response.setCreatedAt(team.getCreatedAt());
        response.setUpdatedAt(team.getUpdatedAt());

        // Fetch and set team members
        List<TeamMember> teamMembers = teamMemberRepository.findByTeamId(team.getId());
        List<com.example.playmatch.api.model.TeamMember> members = teamMembers.stream()
                .map(this::convertToApiTeamMember)
                .collect(Collectors.toList());
        response.setMembers(members);

        return response;
    }

    private TeamSearchResponseItemsInner convertToTeamSearchItem(Team team) {
        TeamSearchResponseItemsInner item = new TeamSearchResponseItemsInner();
        item.setId(team.getId());
        item.setName(team.getName());
        item.setCity(team.getCity());
        if (team.getLogoUrl() != null) {
            try {
                item.setLogoUrl(java.net.URI.create(team.getLogoUrl()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid logo URL for team {}: {}", team.getId(), team.getLogoUrl());
            }
        }
        return item;
    }

    private com.example.playmatch.api.model.TeamMember convertToApiTeamMember(TeamMember member) {
        com.example.playmatch.api.model.TeamMember apiMember = new com.example.playmatch.api.model.TeamMember();

        // Get userId from the User entity relationship if available, otherwise from the userId field
        if (member.getUser() != null) {
            apiMember.setUserId(member.getUser().getId());
            apiMember.setUserName(member.getUser().getName());
        } else if (member.getUserId() != null) {
            apiMember.setUserId(member.getUserId());
        }

        apiMember.setRole(com.example.playmatch.api.model.TeamRole.valueOf(member.getRole().name()));
        apiMember.setJoinedAt(member.getJoinedAt());

        return apiMember;
    }
}
