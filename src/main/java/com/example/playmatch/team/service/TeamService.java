package com.example.playmatch.team.service;

import com.example.playmatch.api.model.*;
import com.example.playmatch.auth.repository.UserRepository;
import com.example.playmatch.team.exception.TeamNotFoundException;
import com.example.playmatch.team.exception.TeamMemberNotFoundException;
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
        TeamMember creatorMember = TeamMember.builder()
                .team(savedTeam)
                .userId(createdByUserId)
                .role(TeamRole.ADMIN)
                .build();
        teamMemberRepository.save(creatorMember);

        return convertToTeamResponse(savedTeam);
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeam(Long teamId) {
        log.info("Fetching team with id: {}", teamId);
        Team team = teamRepository.findByIdAndIsActiveTrue(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));
        return convertToTeamResponse(team);
    }

    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request) {
        log.info("Updating team with id: {}", teamId);
        Team team = teamRepository.findByIdAndIsActiveTrue(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

            team.setName(request.getName());

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
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        team.setIsActive(false);
        teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public TeamSearchResponse searchTeams(String city, String name, Integer limit, Integer offset) {
        log.info("Searching teams with city: {}, name: {}", city, name);

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
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        List<Long> successIds = new ArrayList<>();
        List<BulkOperationResultFailedInner> failed = new ArrayList<>();

        for (Long userId : request.getPlayerIds()) {
            try {
                // Check if user exists
                if (!userRepository.existsById(userId)) {
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
                        .userId(userId)
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

    public void removeTeamMember(Long teamId, Long userId) {
        log.info("Removing member {} from team: {}", userId, teamId);

        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId);
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new TeamMemberNotFoundException(teamId, userId));

        teamMemberRepository.delete(member);
    }

    public void changeMemberRole(Long teamId, Long userId, ChangeRoleRequest request) {
        log.info("Changing role for member {} in team: {}", userId, teamId);

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new TeamMemberNotFoundException(teamId, userId));

        TeamRole newRole = TeamRole.valueOf(request.getRole().name());
        member.setRole(newRole);
        teamMemberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberSearchResponse listTeamMembers(Long teamId, com.example.playmatch.api.model.TeamRole role, Integer limit, Integer offset) {
        log.info("Listing members for team: {}", teamId);

        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId);
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
        response.setCreatedAt(team.getCreatedAt());
        response.setUpdatedAt(team.getUpdatedAt());
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
        apiMember.setUserId(member.getUserId());
        apiMember.setRole(com.example.playmatch.api.model.TeamRole.valueOf(member.getRole().name()));
        apiMember.setJoinedAt(member.getJoinedAt());

        // Note: The TeamMember API model doesn't include player name field
        // Player name would need to be fetched separately if needed by the client

        return apiMember;
    }
}
