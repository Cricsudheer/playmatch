# Event Management Feature - Product Requirements Document (PRD)

**Document Version:** 1.0
**Date:** 2026-01-09
**Status:** Draft
**Author:** Product & Solution Architecture Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Problem Statement](#2-problem-statement)
3. [User Personas & User Stories](#3-user-personas--user-stories)
4. [Feature Requirements](#4-feature-requirements)
5. [User Flows](#5-user-flows)
6. [System Design](#6-system-design)
7. [Database Design](#7-database-design)
8. [API Design](#8-api-design)
9. [Security & Authorization](#9-security--authorization)
10. [Edge Cases & Business Rules](#10-edge-cases--business-rules)
11. [Non-Functional Requirements](#11-non-functional-requirements)
12. [Future Enhancements](#12-future-enhancements)
13. [Implementation Roadmap](#13-implementation-roadmap)

---

## 1. Executive Summary

### 1.1 Overview

The **Event Management** feature enables cricket players to organize, invite, and manage participation for various cricket activities including nets sessions, practice matches, and tournament matches. The feature solves the critical pain point of match organizers (captains/coordinators) who need real-time visibility into player availability before confirming a match.

### 1.2 Key Objectives

1. **Simplify Event Creation** - Enable any player to create cricket events with minimal friction
2. **Streamline RSVP Management** - Allow players to respond to event invitations (Yes/No/Tentative)
3. **Provide Captain Visibility** - Give match organizers real-time view of player availability
4. **Reduce No-Shows** - Improve commitment through confirmations and reminders
5. **Build Team Cohesion** - Facilitate regular practice and match scheduling

### 1.3 Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Event Creation Rate | 50+ events/week | Events created per active user |
| RSVP Response Rate | >80% | Players who respond within 24 hours |
| Match Confirmation Rate | >70% | Events with minimum required players |
| Player Satisfaction (NPS) | >50 | Post-match survey |
| No-Show Rate | <10% | Players who confirmed but didn't show |

---

## 2. Problem Statement

### 2.1 Current Pain Points

**For Match Organizers (Captains/Coordinators):**

1. **No centralized platform** - Currently using WhatsApp groups, leading to:
   - Messages getting lost in chat history
   - No clear visibility on who's available
   - Manual counting and tracking of responses
   - Difficult to plan with uncertain numbers

2. **Last-minute cancellations** - Players confirming verbally but not showing up

3. **Time-consuming coordination** - Hours spent on:
   - Following up with individual players
   - Maintaining separate lists in Notes/Excel
   - Confirming final playing XI

**For Players:**

1. **Missing event notifications** - Events announced in group chats get buried
2. **Unclear commitment tracking** - No way to see their own event history
3. **No reminder mechanism** - Forget about matches they committed to
4. **Difficulty finding matches** - No discovery mechanism for open events

### 2.2 Target Users

1. **Primary**: Cricket captains, team coordinators (20% of user base)
2. **Secondary**: Regular players looking for matches (80% of user base)
3. **Tertiary**: Casual players exploring cricket events

### 2.3 Business Impact

- **Player Retention**: Players leave platforms when match coordination is chaotic
- **Network Effect**: Better coordination = more matches = more active users
- **Premium Feature Potential**: Advanced features (auto-reminders, analytics) for paid tiers
- **Platform Stickiness**: Event history creates lock-in effect

---

## 3. User Personas & User Stories

### 3.1 Persona 1: Rahul (Team Captain)

**Demographics:**
- Age: 28
- Role: Team Captain, Software Engineer
- Experience: 10+ years playing cricket
- Tech-savviness: High

**Goals:**
- Organize weekend matches with minimum 11 confirmed players
- Know player availability 48 hours before match
- Build a reliable pool of players who commit and show up

**Frustrations:**
- Spending 2-3 hours coordinating each match on WhatsApp
- Last-minute dropouts forcing match cancellations
- No data on player reliability

**User Stories:**

```
As a team captain,
I want to create a match event and invite my team members,
So that I can gauge availability before confirming the match booking.

As a team captain,
I want to see real-time responses (Yes/No/Tentative),
So that I can decide whether to proceed with the match or reschedule.

As a team captain,
I want to set a minimum player requirement (e.g., 11 players),
So that the system can auto-notify me when the threshold is met.

As a team captain,
I want to see player attendance history,
So that I can make informed decisions when selecting playing XI.
```

### 3.2 Persona 2: Priya (Regular Player)

**Demographics:**
- Age: 24
- Role: Player, Marketing Professional
- Experience: 5 years playing cricket
- Tech-savviness: Medium

**Goals:**
- Play cricket 2-3 times per month
- Get notified about upcoming matches
- Manage personal calendar and commitments

**Frustrations:**
- Missing match announcements in WhatsApp groups
- Forgetting about matches she committed to
- Guilt from last-minute cancellations

**User Stories:**

```
As a player,
I want to receive notifications when I'm invited to an event,
So that I don't miss opportunities to play.

As a player,
I want to respond with Yes/No/Tentative based on my availability,
So that the organizer knows my commitment level.

As a player,
I want to see all events I've been invited to in one place,
So that I can manage my cricket schedule easily.

As a player,
I want to receive reminders 24 hours before the match,
So that I don't forget my commitment.

As a player,
I want to change my response if my plans change,
So that I can keep the organizer informed.
```

### 3.3 Persona 3: Amit (Casual Player)

**Demographics:**
- Age: 32
- Role: Casual Player, looking for new teams
- Experience: Intermediate player, recently moved to new city
- Tech-savviness: Medium

**Goals:**
- Discover cricket events in his city
- Join practice sessions to network with players
- Build connections to join a regular team

**Frustrations:**
- Don't know how to find local cricket matches
- Not part of any cricket groups
- Difficult to break into established teams

**User Stories:**

```
As a casual player,
I want to discover open/public events in my city,
So that I can find opportunities to play cricket.

As a casual player,
I want to filter events by type (nets/practice/match),
So that I can join events matching my skill level and interest.

As a casual player,
I want to see event details (location, time, skill level),
So that I can decide if it's suitable for me.
```

---

## 4. Feature Requirements

### 4.1 Must-Have (MVP) Features

#### 4.1.1 Event Creation

**Functional Requirements:**

- [ ] **Event Types** - Support three event types:
  - Nets Session (practice/training)
  - Practice Match (friendly match)
  - Tournament Match (competitive match)

- [ ] **Event Details** - Capture essential event information:
  - Title (required, max 120 chars)
  - Description (optional, max 2000 chars)
  - Event Type (required, enum)
  - Date & Time (required, must be future date)
  - Location/Venue (required, max 200 chars)
  - City (required, for discovery)
  - Duration (optional, default varies by type)
  - Maximum participants (optional, default based on event type)

- [ ] **Team Association** - Optionally link event to a team
  - If team-linked: Auto-invite all team members
  - If standalone: Manual invite or public discovery

- [ ] **Visibility Settings**:
  - **Private**: Invite-only, not discoverable
  - **Team-Only**: Visible to team members
  - **Public**: Discoverable by all users in same city

#### 4.1.2 Invitation Management

- [ ] **Invite Mechanisms**:
  - Bulk invite team members (if team-linked event)
  - Search and invite individual players by name/email
  - Share event link for external sharing

- [ ] **Invitation Tracking**:
  - Track invitation status (Invited, Viewed, Responded)
  - Show invite timestamp
  - Resend invitations to non-responders

#### 4.1.3 RSVP System

- [ ] **Response Types**:
  - **YES** - Confirmed attendance (green indicator)
  - **NO** - Cannot attend (red indicator)
  - **TENTATIVE** - Maybe attending (yellow indicator)

- [ ] **RSVP Features**:
  - One-click response from notification
  - Optional comment when responding (e.g., "Will be 30 mins late")
  - Ability to change response until event start time
  - Track response timestamp

- [ ] **Response Visibility**:
  - Event creator sees all responses in real-time
  - Players see summary (e.g., "12 Yes, 3 No, 5 Tentative")
  - Display participant names grouped by response type

#### 4.1.4 Captain Dashboard

- [ ] **Event Overview**:
  - List all events created by the captain
  - Filter by status (Upcoming, Past, Cancelled)
  - Search by event name/date

- [ ] **Participant Summary** (per event):
  - Total invited: X players
  - Confirmed (YES): X players (with names)
  - Declined (NO): X players (with names)
  - Tentative: X players (with names)
  - No Response: X players (with names)
  - Minimum required indicator (e.g., "11/15 confirmed")

- [ ] **Real-time Updates**:
  - Live counter updates when players respond
  - Visual indicator when minimum threshold met
  - Notification when key players respond

#### 4.1.5 Player Event View

- [ ] **My Events Tab**:
  - Upcoming events I'm invited to
  - Past events I participated in
  - Filter by response status (Confirmed/Tentative/Declined/Pending)

- [ ] **Event Discovery**:
  - Browse public events in my city
  - Filter by event type, date, distance
  - See participant count and availability

- [ ] **Event Details Page**:
  - Full event information
  - Participant list (names, response status)
  - Location map integration (future)
  - Comment/discussion section (future)

#### 4.1.6 Notifications

- [ ] **Event Invitation** - Notify when invited to an event
- [ ] **RSVP Update** - Notify organizer when player responds
- [ ] **Event Reminder** - Remind confirmed players 24 hours before
- [ ] **Minimum Met** - Notify organizer when minimum players confirmed
- [ ] **Event Cancellation** - Notify all participants if event is cancelled

**Notification Channels:**
- In-app notifications (MVP)
- Push notifications (future)
- Email notifications (future)
- WhatsApp notifications (future, premium feature)

### 4.2 Should-Have (Post-MVP) Features

- [ ] Recurring events (weekly nets sessions)
- [ ] Waiting list when event is full
- [ ] Player ratings and reviews post-event
- [ ] Event analytics (attendance trends, player reliability scores)
- [ ] Calendar integration (Google Calendar, iCal)
- [ ] Weather integration and auto-suggestions
- [ ] Payment collection for match fees
- [ ] Equipment checklist (bats, balls, stumps needed)

### 4.3 Nice-to-Have (Future) Features

- [ ] AI-powered best time suggestion based on team availability
- [ ] Automatic team selection based on player roles and availability
- [ ] Match scorekeeping integration
- [ ] Live match updates and commentary
- [ ] Video highlights upload and sharing
- [ ] Sponsor/venue partnerships for event hosting

---

## 5. User Flows

### 5.1 Event Creation Flow (Captain)

```
[Start] → Captain clicks "Create Event"
   ↓
[Event Type Selection]
   ├─ Nets Session
   ├─ Practice Match
   └─ Tournament Match
   ↓
[Fill Event Details]
   ├─ Title (required)
   ├─ Date & Time (required)
   ├─ Location (required)
   ├─ Description (optional)
   ├─ Max Participants (optional)
   └─ Link to Team (optional)
   ↓
[Visibility Settings]
   ├─ Private (invite-only)
   ├─ Team-Only
   └─ Public
   ↓
[Invite Players]
   ├─ If team-linked → Auto-select all team members
   ├─ Manual selection from player list
   └─ Skip (can invite later)
   ↓
[Review & Create]
   ↓
[Event Created Successfully]
   ├─ Show event details page
   ├─ Show shareable link
   └─ Send invitations to selected players
   ↓
[End]
```

**Validation Rules:**
- Event date must be in the future (minimum 2 hours from now)
- Location must be provided
- If team-linked, user must be ADMIN or COORDINATOR of that team
- Maximum participants must be > 0 if specified
- Title must be unique per creator per day (prevent duplicates)

**Success Scenario:**
```
Captain Rahul creates "Sunday Match - Cubbon Park"
→ Selects team "Bangalore Warriors"
→ Sets visibility to "Team-Only"
→ All 20 team members auto-selected
→ Event created, invitations sent
→ Rahul sees event dashboard with 0 responses initially
```

**Error Scenarios:**
- **Invalid date**: "Event date must be at least 2 hours in the future"
- **Not authorized**: "You must be a team admin to create team events"
- **Duplicate event**: "You already have an event with this title on this date"

---

### 5.2 RSVP Flow (Player)

```
[Start] → Player receives event invitation notification
   ↓
[Opens Notification]
   ↓
[Event Details Page]
   ├─ View event title, date, time, location
   ├─ View organizer details
   ├─ View current participant summary (X Yes, Y No, Z Tentative)
   └─ View other confirmed players
   ↓
[RSVP Decision]
   ├─ YES (I will attend)
   ├─ NO (Cannot attend)
   └─ TENTATIVE (Maybe)
   ↓
[Optional: Add Comment]
   ├─ "Will be 30 mins late"
   ├─ "Need a ride"
   └─ Skip
   ↓
[Submit Response]
   ↓
[Confirmation Message]
   ├─ "Your response has been recorded"
   ├─ Add to calendar prompt
   └─ Return to event list
   ↓
[Organizer Notified]
   ↓
[End]
```

**Business Rules:**
- Player can change response until event start time
- Each response change triggers notification to organizer
- If event is full and player responds YES, add to waiting list
- If player previously responded NO and changes to YES, move from declined to confirmed list

**Success Scenario:**
```
Player Priya receives invitation for "Sunday Match"
→ Opens event details
→ Sees 10 players already confirmed
→ Clicks "YES, I will attend"
→ Response recorded
→ Captain Rahul receives notification: "Priya confirmed for Sunday Match (11/15 confirmed)"
```

**Edge Cases:**
- **Event full**: "Event is full (20/20). Join waiting list?"
- **Event in past**: "This event has already started. You cannot respond."
- **Event cancelled**: "This event has been cancelled by the organizer."

---

### 5.3 Captain Visibility Flow

```
[Start] → Captain opens "My Events" tab
   ↓
[Event List View]
   ├─ Upcoming Events (sorted by date)
   │   └─ For each event:
   │       ├─ Event title
   │       ├─ Date & time
   │       ├─ Response summary (12 Yes, 3 No, 5 Tentative)
   │       └─ Minimum met indicator (✓ or ✗)
   └─ Past Events
   ↓
[Select Event] → "Sunday Match - Cubbon Park"
   ↓
[Event Dashboard]
   ├─ Event Details
   │   ├─ Date, Time, Location
   │   └─ Edit/Cancel buttons
   │
   ├─ Response Summary (Visual)
   │   ├─ 12 Confirmed (GREEN) - 11 minimum required ✓
   │   ├─ 3 Declined (RED)
   │   ├─ 5 Tentative (YELLOW)
   │   └─ 5 No Response (GREY)
   │
   ├─ Participant Lists (Expandable)
   │   ├─ Confirmed Players
   │   │   └─ For each: Name, Response time, Comment, Attendance history
   │   ├─ Tentative Players
   │   ├─ Declined Players
   │   └─ Pending Response
   │       └─ [Resend Invitation] button
   │
   ├─ Actions
   │   ├─ Send Reminder to All
   │   ├─ Send Reminder to Pending
   │   ├─ Edit Event
   │   ├─ Cancel Event
   │   └─ Mark as Completed
   │
   └─ Activity Timeline
       ├─ "Priya confirmed - 2 mins ago"
       ├─ "Amit changed response to NO - 1 hour ago"
       └─ "Event created - 3 days ago"
   ↓
[End]
```

**Key Features:**

1. **Visual Indicators**:
   - Green checkmark when minimum players met
   - Red alert if event date approaching and minimum not met
   - Yellow warning for high tentative count

2. **Quick Actions**:
   - One-click reminder to non-responders
   - Quick access to participant contact info
   - Export participant list (CSV/PDF)

3. **Real-time Updates**:
   - Live counter updates (WebSocket/polling)
   - Push notification when responses received
   - Activity feed showing recent changes

**Success Scenario:**
```
Captain Rahul opens event dashboard for Sunday match
→ Sees 12 confirmed players (minimum 11 required ✓)
→ Reviews confirmed player list:
   - 6 batsmen, 4 bowlers, 2 all-rounders
→ Sees 5 players pending response
→ Clicks "Send Reminder to Pending"
→ Notifications sent to 5 players
→ Returns to dashboard
```

---

### 5.4 Event Discovery Flow (Casual Player)

```
[Start] → Player opens "Discover Events" tab
   ↓
[Event Discovery Page]
   ├─ Filter Options
   │   ├─ City (auto-populated from profile)
   │   ├─ Event Type (All/Nets/Practice/Tournament)
   │   ├─ Date Range (Today/This Week/This Month)
   │   └─ Distance (Within 5km/10km/20km)
   │
   ├─ Event List (Cards)
   │   └─ For each event:
   │       ├─ Event title
   │       ├─ Date, time, location
   │       ├─ Event type badge
   │       ├─ Participant count (15/20 confirmed)
   │       ├─ Organizer name & rating
   │       └─ [View Details] button
   ↓
[Select Event] → "Nets Session - HSR Layout"
   ↓
[Event Details Page]
   ├─ Full event information
   ├─ Organizer profile (name, rating, events hosted)
   ├─ Participant list (if public)
   ├─ Location map
   └─ [Request to Join] button
   ↓
[Request to Join]
   ├─ If auto-accept enabled → Instant confirmation
   └─ If requires approval → Request sent to organizer
   ↓
[Confirmation/Pending Message]
   ↓
[End]
```

**Business Rules:**
- Only show public events or team events for teams user belongs to
- Filter events by user's profile city by default
- Show only future events (past events in separate "Past Events" section)
- Display "Event Full" badge if max participants reached

**Success Scenario:**
```
Player Amit (new to Bangalore) opens Discover Events
→ Sees 5 public events in Bangalore this week
→ Filters by "Nets Session"
→ Finds "Sunday Nets - Cubbon Park"
→ Clicks "Request to Join"
→ Organizer receives request, auto-accepts
→ Amit receives confirmation notification
→ Event added to "My Events" tab
```

---

### 5.5 Event Modification Flow

```
[Start] → Captain opens event dashboard
   ↓
[Event Dashboard]
   ↓
[Clicks "Edit Event"]
   ↓
[Edit Event Form]
   ├─ Modify title
   ├─ Modify date/time
   ├─ Modify location
   ├─ Modify description
   ├─ Modify max participants
   └─ Cannot modify: Event type, Team association
   ↓
[Save Changes]
   ↓
[Validation]
   ├─ If date changed → Check if new date is valid
   ├─ If location changed → Valid location
   └─ If max participants reduced → Check if current confirmed > new max
   ↓
[Update Event]
   ↓
[Notify All Participants]
   └─ "Event details updated: [Summary of changes]"
   ↓
[End]

[Alternative: Cancel Event]
   ↓
[Confirmation Dialog]
   └─ "Are you sure? This will notify all X participants."
   ↓
[Cancel Event]
   ├─ Set event status to CANCELLED
   ├─ Notify all participants
   └─ Allow organizer to add cancellation reason
   ↓
[End]
```

**Business Rules:**
- Cannot edit event within 2 hours of start time
- Cannot change event type or team association after creation
- If date/time changed, all participants notified
- If event cancelled, cannot be reactivated (must create new event)
- Cancelled events remain in history for reference

---

## 6. System Design

### 6.1 Architecture Overview

Following the existing PlayMatch architecture patterns:

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
│  (Web App, Mobile App, Postman)                                 │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTPS
                                │ Bearer Token Auth
┌───────────────────────────────▼─────────────────────────────────┐
│                     API GATEWAY / NGINX                          │
│  - Rate Limiting                                                 │
│  - CORS                                                          │
│  - SSL Termination                                               │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                   SPRING BOOT APPLICATION                        │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              SECURITY LAYER                                 │ │
│  │  - JwtAuthenticationFilter                                  │ │
│  │  - SecurityContext                                          │ │
│  │  - CurrentUser utility                                      │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              CONTROLLER LAYER                               │ │
│  │  - EventController (implements EventApi)                    │ │
│  │  - Request validation (@Valid)                              │ │
│  │  - Authorization checks                                     │ │
│  │  - HTTP response mapping                                    │ │
│  └───────────────────┬────────────────────────────────────────┘ │
│                      │                                           │
│  ┌───────────────────▼────────────────────────────────────────┐ │
│  │              SERVICE LAYER                                  │ │
│  │  - EventService                                             │ │
│  │  - EventParticipantService                                  │ │
│  │  - NotificationService (existing)                           │ │
│  │  - Business logic & validation                              │ │
│  │  - Transaction management                                   │ │
│  └───────────────────┬────────────────────────────────────────┘ │
│                      │                                           │
│  ┌───────────────────▼────────────────────────────────────────┐ │
│  │              REPOSITORY LAYER                               │ │
│  │  - EventRepository                                          │ │
│  │  - EventParticipantRepository                               │ │
│  │  - Custom JPQL queries                                      │ │
│  └───────────────────┬────────────────────────────────────────┘ │
│                      │                                           │
└──────────────────────┼───────────────────────────────────────────┘
                       │
       ┌───────────────┴───────────────┐
       │                               │
┌──────▼──────┐              ┌─────────▼────────┐
│  PostgreSQL │              │      Redis       │
│             │              │                  │
│  - Event    │              │  - Rate limiting │
│  - EventPar │              │  - Caching       │
│  - User     │              │  - Sessions      │
│  - Team     │              │                  │
└─────────────┘              └──────────────────┘
```

### 6.2 Module Structure

```
com.example.playmatch/
└── event/
    ├── controller/
    │   └── EventController.java
    │
    ├── service/
    │   ├── EventService.java
    │   ├── EventParticipantService.java
    │   └── impl/
    │       ├── EventServiceImpl.java
    │       └── EventParticipantServiceImpl.java
    │
    ├── repository/
    │   ├── EventRepository.java
    │   └── EventParticipantRepository.java
    │
    ├── model/
    │   ├── Event.java
    │   ├── EventParticipant.java
    │   └── enums/
    │       ├── EventType.java
    │       ├── EventStatus.java
    │       ├── EventVisibility.java
    │       └── ResponseStatus.java
    │
    └── exception/
        ├── EventException.java
        └── EventError.java
```

### 6.3 Component Responsibilities

#### EventController
- Handles HTTP requests/responses
- Implements EventApi (generated from OpenAPI spec)
- Validates request DTOs
- Extracts current user from SecurityContext
- Authorization checks (event creator, team admin)
- Maps service responses to HTTP responses

#### EventService
- Core business logic for event management
- Event creation, update, cancellation
- Event search and filtering
- Permission validation (can user edit/delete event?)
- Integration with TeamService for team-based events

#### EventParticipantService
- Manages participant invitations and RSVPs
- Tracks response status (YES/NO/TENTATIVE)
- Participant list management
- Waiting list management
- Response change tracking

#### EventRepository
- CRUD operations for Event entity
- Custom queries for event search/filtering
- Paginated results for event lists
- Statistics queries (count by status, etc.)

#### EventParticipantRepository
- CRUD operations for EventParticipant entity
- Queries for participant lists grouped by response status
- Check participant existence and uniqueness
- Response summary aggregations

---

## 7. Database Design

### 7.1 Entity Relationship Diagram (ERD)

```
┌─────────────────────────────┐
│         app_user            │
│─────────────────────────────│
│ id (PK)                     │
│ name                        │
│ email                       │
│ password_hash               │
│ created_at                  │
└──────────────┬──────────────┘
               │
               │ 1
               │
               │ *
┌──────────────▼──────────────┐         ┌─────────────────────────────┐
│     player_profile          │         │           team              │
│─────────────────────────────│         │─────────────────────────────│
│ id (PK)                     │         │ id (PK)                     │
│ user_id (FK) UNIQUE         │         │ name                        │
│ full_name                   │         │ city                        │
│ city                        │         │ created_by_user_id (FK)     │
│ primary_role                │         │ is_active                   │
│ created_at                  │         │ created_at                  │
└─────────────────────────────┘         └──────────────┬──────────────┘
                                                       │
                                                       │ 1
                                                       │
                                                       │ *
┌─────────────────────────────────────────────────────▼──────────────┐
│                            event                                    │
│────────────────────────────────────────────────────────────────────│
│ id (PK) BIGSERIAL                                                   │
│ title VARCHAR(120) NOT NULL                                         │
│ description TEXT                                                    │
│ event_type VARCHAR(50) NOT NULL  -- NETS_SESSION, PRACTICE_MATCH,  │
│                                     TOURNAMENT_MATCH                │
│ event_status VARCHAR(50) NOT NULL -- DRAFT, PUBLISHED, CANCELLED,  │
│                                      COMPLETED                      │
│ event_date TIMESTAMP WITH TIME ZONE NOT NULL                        │
│ duration_minutes INTEGER                                            │
│ location VARCHAR(200) NOT NULL                                      │
│ city VARCHAR(100) NOT NULL                                          │
│ visibility VARCHAR(50) NOT NULL  -- PRIVATE, TEAM_ONLY, PUBLIC     │
│ created_by_user_id BIGINT NOT NULL (FK → app_user.id)              │
│ team_id BIGINT (FK → team.id) -- NULL if standalone event          │
│ max_participants INTEGER                                            │
│ min_participants INTEGER -- For auto-confirmation logic             │
│ auto_accept_requests BOOLEAN DEFAULT false -- For public events    │
│ is_active BOOLEAN NOT NULL DEFAULT true                             │
│ created_at TIMESTAMP WITH TIME ZONE NOT NULL                        │
│ updated_at TIMESTAMP WITH TIME ZONE NOT NULL                        │
│ cancelled_at TIMESTAMP WITH TIME ZONE                               │
│ cancellation_reason TEXT                                            │
│                                                                     │
│ INDEXES:                                                            │
│   idx_event_creator (created_by_user_id)                            │
│   idx_event_team (team_id)                                          │
│   idx_event_date (event_date)                                       │
│   idx_event_city_type (city, event_type)                            │
│   idx_event_status (event_status)                                   │
│   idx_event_visibility (visibility)                                 │
│                                                                     │
│ CONSTRAINTS:                                                        │
│   fk_event_creator FOREIGN KEY (created_by_user_id)                 │
│       REFERENCES app_user(id) ON DELETE RESTRICT                    │
│   fk_event_team FOREIGN KEY (team_id)                               │
│       REFERENCES team(id) ON DELETE SET NULL                        │
│   check_event_date CHECK (event_date > created_at)                  │
│   check_max_participants CHECK (max_participants > 0)               │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              │ 1
                              │
                              │ *
┌─────────────────────────────▼───────────────────────────────────────┐
│                       event_participant                              │
│─────────────────────────────────────────────────────────────────────│
│ id (PK) BIGSERIAL                                                    │
│ event_id BIGINT NOT NULL (FK → event.id)                            │
│ user_id BIGINT NOT NULL (FK → app_user.id)                          │
│ response_status VARCHAR(50) NOT NULL -- INVITED, YES, NO, TENTATIVE │
│ response_comment TEXT                                                │
│ invited_at TIMESTAMP WITH TIME ZONE NOT NULL                         │
│ responded_at TIMESTAMP WITH TIME ZONE                                │
│ updated_at TIMESTAMP WITH TIME ZONE NOT NULL                         │
│ is_active BOOLEAN NOT NULL DEFAULT true                              │
│                                                                      │
│ INDEXES:                                                             │
│   idx_event_participant_event (event_id)                             │
│   idx_event_participant_user (user_id)                               │
│   idx_event_participant_status (event_id, response_status)           │
│                                                                      │
│ CONSTRAINTS:                                                         │
│   uq_event_user UNIQUE (event_id, user_id)                           │
│   fk_event_participant_event FOREIGN KEY (event_id)                  │
│       REFERENCES event(id) ON DELETE CASCADE                         │
│   fk_event_participant_user FOREIGN KEY (user_id)                    │
│       REFERENCES app_user(id) ON DELETE CASCADE                      │
└──────────────────────────────────────────────────────────────────────┘
```

### 7.2 Table Definitions (PostgreSQL DDL)

```sql
-- =====================================================
-- EVENT TABLE
-- =====================================================
CREATE TABLE event (
    id                      BIGSERIAL PRIMARY KEY,
    title                   VARCHAR(120) NOT NULL,
    description             TEXT,
    event_type              VARCHAR(50) NOT NULL,
    event_status            VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    event_date              TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes        INTEGER,
    location                VARCHAR(200) NOT NULL,
    city                    VARCHAR(100) NOT NULL,
    visibility              VARCHAR(50) NOT NULL DEFAULT 'PRIVATE',
    created_by_user_id      BIGINT NOT NULL,
    team_id                 BIGINT,
    max_participants        INTEGER,
    min_participants        INTEGER,
    auto_accept_requests    BOOLEAN NOT NULL DEFAULT false,
    is_active               BOOLEAN NOT NULL DEFAULT true,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at            TIMESTAMP WITH TIME ZONE,
    cancellation_reason     TEXT,

    -- Foreign Keys
    CONSTRAINT fk_event_creator
        FOREIGN KEY (created_by_user_id)
        REFERENCES app_user(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_event_team
        FOREIGN KEY (team_id)
        REFERENCES team(id)
        ON DELETE SET NULL,

    -- Check Constraints
    CONSTRAINT check_event_date
        CHECK (event_date > created_at),

    CONSTRAINT check_max_participants
        CHECK (max_participants IS NULL OR max_participants > 0),

    CONSTRAINT check_min_participants
        CHECK (min_participants IS NULL OR min_participants > 0),

    CONSTRAINT check_min_max_participants
        CHECK (min_participants IS NULL OR max_participants IS NULL
               OR min_participants <= max_participants),

    CONSTRAINT check_event_type
        CHECK (event_type IN ('NETS_SESSION', 'PRACTICE_MATCH', 'TOURNAMENT_MATCH')),

    CONSTRAINT check_event_status
        CHECK (event_status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED')),

    CONSTRAINT check_visibility
        CHECK (visibility IN ('PRIVATE', 'TEAM_ONLY', 'PUBLIC'))
);

-- Indexes for performance
CREATE INDEX idx_event_creator ON event(created_by_user_id);
CREATE INDEX idx_event_team ON event(team_id);
CREATE INDEX idx_event_date ON event(event_date);
CREATE INDEX idx_event_city_type ON event(city, event_type);
CREATE INDEX idx_event_status ON event(event_status);
CREATE INDEX idx_event_visibility ON event(visibility);
CREATE INDEX idx_event_active_date ON event(is_active, event_date)
    WHERE is_active = true;

-- Composite index for common queries
CREATE INDEX idx_event_city_date_status ON event(city, event_date, event_status)
    WHERE is_active = true;

-- =====================================================
-- EVENT_PARTICIPANT TABLE
-- =====================================================
CREATE TABLE event_participant (
    id                  BIGSERIAL PRIMARY KEY,
    event_id            BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,
    response_status     VARCHAR(50) NOT NULL DEFAULT 'INVITED',
    response_comment    TEXT,
    invited_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at        TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active           BOOLEAN NOT NULL DEFAULT true,

    -- Foreign Keys
    CONSTRAINT fk_event_participant_event
        FOREIGN KEY (event_id)
        REFERENCES event(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_event_participant_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,

    -- Check Constraints
    CONSTRAINT check_response_status
        CHECK (response_status IN ('INVITED', 'YES', 'NO', 'TENTATIVE')),

    -- Unique Constraint
    CONSTRAINT uq_event_user
        UNIQUE (event_id, user_id)
);

-- Indexes for performance
CREATE INDEX idx_event_participant_event ON event_participant(event_id);
CREATE INDEX idx_event_participant_user ON event_participant(user_id);
CREATE INDEX idx_event_participant_status ON event_participant(event_id, response_status);
CREATE INDEX idx_event_participant_active ON event_participant(event_id, is_active)
    WHERE is_active = true;

-- Index for user's upcoming events
CREATE INDEX idx_user_upcoming_events ON event_participant(user_id, event_id)
    WHERE is_active = true AND response_status IN ('YES', 'TENTATIVE');

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER event_updated_at
    BEFORE UPDATE ON event
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER event_participant_updated_at
    BEFORE UPDATE ON event_participant
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Auto-set responded_at when response_status changes from INVITED
CREATE OR REPLACE FUNCTION set_responded_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.response_status != 'INVITED' AND OLD.response_status = 'INVITED' THEN
        NEW.responded_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER event_participant_responded_at
    BEFORE UPDATE ON event_participant
    FOR EACH ROW
    EXECUTE FUNCTION set_responded_at();
```

### 7.3 Enum Definitions

```java
// EventType.java
public enum EventType {
    NETS_SESSION,       // Practice/training session
    PRACTICE_MATCH,     // Friendly match
    TOURNAMENT_MATCH    // Competitive match
}

// EventStatus.java
public enum EventStatus {
    DRAFT,              // Created but not published
    PUBLISHED,          // Active and visible to invitees
    CANCELLED,          // Cancelled by organizer
    COMPLETED           // Event finished
}

// EventVisibility.java
public enum EventVisibility {
    PRIVATE,            // Invite-only, not discoverable
    TEAM_ONLY,          // Visible to team members only
    PUBLIC              // Discoverable by all users in city
}

// ResponseStatus.java
public enum ResponseStatus {
    INVITED,            // Invited but not responded
    YES,                // Confirmed attendance
    NO,                 // Declined
    TENTATIVE           // Maybe attending
}
```

### 7.4 Sample Data & Queries

#### Sample Insert Queries

```sql
-- Create a team event
INSERT INTO event (
    title, description, event_type, event_status, event_date,
    duration_minutes, location, city, visibility, created_by_user_id,
    team_id, max_participants, min_participants, auto_accept_requests
) VALUES (
    'Sunday Match - Cubbon Park',
    'Regular weekend match. Bring your own kit.',
    'PRACTICE_MATCH',
    'PUBLISHED',
    '2026-01-12 09:00:00+05:30',
    180,
    'Cubbon Park Cricket Ground, Gate 3',
    'Bengaluru',
    'TEAM_ONLY',
    1,  -- Captain's user ID
    5,  -- Team ID
    22,
    11,
    false
);

-- Invite team members
INSERT INTO event_participant (event_id, user_id, response_status, invited_at)
SELECT 1, user_id, 'INVITED', CURRENT_TIMESTAMP
FROM team_member
WHERE team_id = 5 AND is_active = true;

-- Player responds YES
UPDATE event_participant
SET response_status = 'YES', response_comment = 'I will be there!'
WHERE event_id = 1 AND user_id = 10;
```

#### Common Queries

```sql
-- 1. Get all events created by a user
SELECT * FROM event
WHERE created_by_user_id = ? AND is_active = true
ORDER BY event_date DESC;

-- 2. Get upcoming events for a user (invited to)
SELECT e.*, ep.response_status, ep.response_comment
FROM event e
INNER JOIN event_participant ep ON e.id = ep.event_id
WHERE ep.user_id = ?
  AND ep.is_active = true
  AND e.is_active = true
  AND e.event_status = 'PUBLISHED'
  AND e.event_date > CURRENT_TIMESTAMP
ORDER BY e.event_date ASC;

-- 3. Get participant summary for an event
SELECT
    response_status,
    COUNT(*) as count
FROM event_participant
WHERE event_id = ? AND is_active = true
GROUP BY response_status;

-- 4. Get confirmed players for an event
SELECT u.id, u.name, pp.full_name, ep.responded_at
FROM event_participant ep
INNER JOIN app_user u ON ep.user_id = u.id
LEFT JOIN player_profile pp ON u.id = pp.user_id
WHERE ep.event_id = ?
  AND ep.response_status = 'YES'
  AND ep.is_active = true
ORDER BY ep.responded_at ASC;

-- 5. Discover public events in a city
SELECT e.*,
       u.name as creator_name,
       COUNT(ep.id) FILTER (WHERE ep.response_status = 'YES') as confirmed_count
FROM event e
INNER JOIN app_user u ON e.created_by_user_id = u.id
LEFT JOIN event_participant ep ON e.id = ep.event_id AND ep.is_active = true
WHERE e.visibility = 'PUBLIC'
  AND e.city = ?
  AND e.event_status = 'PUBLISHED'
  AND e.event_date > CURRENT_TIMESTAMP
  AND e.is_active = true
GROUP BY e.id, u.name
HAVING COUNT(ep.id) FILTER (WHERE ep.response_status = 'YES') < COALESCE(e.max_participants, 9999)
ORDER BY e.event_date ASC
LIMIT 20;

-- 6. Check if user has already responded to event
SELECT response_status
FROM event_participant
WHERE event_id = ? AND user_id = ? AND is_active = true;

-- 7. Get events needing reminder (24 hours before)
SELECT e.id, e.title, e.event_date
FROM event e
WHERE e.event_status = 'PUBLISHED'
  AND e.is_active = true
  AND e.event_date BETWEEN CURRENT_TIMESTAMP + INTERVAL '23 hours'
                       AND CURRENT_TIMESTAMP + INTERVAL '25 hours';

-- 8. Get player attendance statistics
SELECT
    u.id,
    u.name,
    COUNT(*) as events_invited,
    COUNT(*) FILTER (WHERE ep.response_status = 'YES') as events_confirmed,
    COUNT(*) FILTER (WHERE ep.response_status = 'NO') as events_declined,
    COUNT(*) FILTER (WHERE ep.response_status = 'TENTATIVE') as events_tentative,
    COUNT(*) FILTER (WHERE ep.response_status = 'INVITED') as events_no_response,
    ROUND(
        COUNT(*) FILTER (WHERE ep.response_status = 'YES')::NUMERIC /
        NULLIF(COUNT(*), 0) * 100,
        2
    ) as response_rate_percentage
FROM app_user u
INNER JOIN event_participant ep ON u.id = ep.user_id
INNER JOIN event e ON ep.event_id = e.id
WHERE e.created_by_user_id = ?  -- Captain's events
  AND e.event_date < CURRENT_TIMESTAMP  -- Past events only
  AND ep.is_active = true
GROUP BY u.id, u.name
ORDER BY response_rate_percentage DESC;
```

### 7.5 Database Constraints & Business Rules

#### Constraint Summary

| Constraint Type | Rule | Rationale |
|----------------|------|-----------|
| **Unique** | (event_id, user_id) in event_participant | A user can only respond once per event |
| **Foreign Key** | event.created_by_user_id → app_user.id (RESTRICT) | Cannot delete user who created events |
| **Foreign Key** | event.team_id → team.id (SET NULL) | If team deleted, event becomes standalone |
| **Foreign Key** | event_participant.event_id → event.id (CASCADE) | Delete participants when event is deleted |
| **Foreign Key** | event_participant.user_id → app_user.id (CASCADE) | Delete participations when user is deleted |
| **Check** | event_date > created_at | Event must be in the future at creation |
| **Check** | max_participants > 0 | Maximum participants must be positive |
| **Check** | min_participants <= max_participants | Logical constraint |
| **Check** | event_type in (allowed values) | Enum validation at DB level |
| **Check** | response_status in (allowed values) | Enum validation at DB level |

#### Soft Delete Strategy

- Both `event` and `event_participant` use `is_active` boolean for soft deletes
- Cancelled events set `event_status = 'CANCELLED'` and `cancelled_at = timestamp`
- Queries always filter by `is_active = true` unless explicitly querying deleted records
- Soft deletes preserve data for historical analysis and auditing

#### Data Retention Policy

```sql
-- Archive events older than 1 year
UPDATE event
SET is_active = false
WHERE event_date < CURRENT_TIMESTAMP - INTERVAL '1 year'
  AND event_status = 'COMPLETED'
  AND is_active = true;

-- Hard delete cancelled events older than 6 months (optional)
DELETE FROM event
WHERE event_status = 'CANCELLED'
  AND cancelled_at < CURRENT_TIMESTAMP - INTERVAL '6 months';
```

---

## 8. API Design

### 8.1 OpenAPI Specification

Following the existing pattern, define API in `/src/main/resources/api/api-docs.yaml`:

```yaml
# =====================================================
# EVENT MANAGEMENT API
# =====================================================

/api/events:
  post:
    summary: Create a new event
    operationId: createEvent
    tags: [Events]
    security:
      - bearerAuth: []
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/CreateEventRequest'
    responses:
      '201':
        description: Event created successfully
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventResponse'
      '400':
        $ref: '#/components/responses/BadRequest'
      '401':
        $ref: '#/components/responses/Unauthorized'
      '403':
        $ref: '#/components/responses/Forbidden'
      '422':
        $ref: '#/components/responses/ValidationFailed'

  get:
    summary: Search/list events
    operationId: searchEvents
    tags: [Events]
    parameters:
      - name: city
        in: query
        schema:
          type: string
      - name: eventType
        in: query
        schema:
          $ref: '#/components/schemas/EventType'
      - name: visibility
        in: query
        schema:
          $ref: '#/components/schemas/EventVisibility'
      - name: startDate
        in: query
        schema:
          type: string
          format: date-time
      - name: endDate
        in: query
        schema:
          type: string
          format: date-time
      - name: createdBy
        in: query
        description: Filter events created by specific user ID
        schema:
          type: integer
          format: int64
      - name: teamId
        in: query
        description: Filter events for specific team
        schema:
          type: integer
          format: int64
      - name: limit
        in: query
        schema:
          type: integer
          default: 20
          minimum: 1
          maximum: 100
      - name: offset
        in: query
        schema:
          type: integer
          default: 0
          minimum: 0
    responses:
      '200':
        description: Events list
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventSearchResponse'

/api/events/{eventId}:
  get:
    summary: Get event details
    operationId: getEvent
    tags: [Events]
    parameters:
      - name: eventId
        in: path
        required: true
        schema:
          type: integer
          format: int64
    responses:
      '200':
        description: Event details
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventDetailResponse'
      '404':
        $ref: '#/components/responses/NotFound'

  put:
    summary: Update event
    operationId: updateEvent
    tags: [Events]
    security:
      - bearerAuth: []
    parameters:
      - name: eventId
        in: path
        required: true
        schema:
          type: integer
          format: int64
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/UpdateEventRequest'
    responses:
      '200':
        description: Event updated
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventResponse'
      '403':
        $ref: '#/components/responses/Forbidden'
      '404':
        $ref: '#/components/responses/NotFound'

  delete:
    summary: Cancel event (soft delete)
    operationId: cancelEvent
    tags: [Events]
    security:
      - bearerAuth: []
    parameters:
      - name: eventId
        in: path
        required: true
        schema:
          type: integer
          format: int64
    requestBody:
      required: false
      content:
        application/json:
          schema:
            type: object
            properties:
              reason:
                type: string
                maxLength: 500
    responses:
      '204':
        description: Event cancelled
      '403':
        $ref: '#/components/responses/Forbidden'
      '404':
        $ref: '#/components/responses/NotFound'

/api/events/{eventId}/participants:
  post:
    summary: Invite participants to event
    operationId: inviteParticipants
    tags: [Events]
    security:
      - bearerAuth: []
    parameters:
      - name: eventId
        in: path
        required: true
        schema:
          type: integer
          format: int64
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/InviteParticipantsRequest'
    responses:
      '200':
        description: Participants invited
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/InviteParticipantsResponse'
      '403':
        $ref: '#/components/responses/Forbidden'
      '404':
        $ref: '#/components/responses/NotFound'

  get:
    summary: Get event participants
    operationId: getEventParticipants
    tags: [Events]
    security:
      - bearerAuth: []
    parameters:
      - name: eventId
        in: path
        required: true
        schema:
          type: integer
          format: int64
      - name: responseStatus
        in: query
        schema:
          $ref: '#/components/schemas/ResponseStatus'
    responses:
      '200':
        description: Participant list
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ParticipantListResponse'

/api/events/{eventId}/respond:
  post:
    summary: Respond to event invitation (RSVP)
    operationId: respondToEvent
    tags: [Events]
    security:
      - bearerAuth: []
    parameters:
      - name: eventId
        in: path
        required: true
        schema:
          type: integer
          format: int64
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/EventResponseRequest'
    responses:
      '200':
        description: Response recorded
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MessageResponse'
      '400':
        description: Event has passed or response not allowed
      '404':
        $ref: '#/components/responses/NotFound'

/api/events/{eventId}/participants/{userId}:
  delete:
    summary: Remove participant from event
    operationId: removeParticipant
    tags: [Events]
    security:
      - bearerAuth: []
    parameters:
      - name: eventId
        in: path
        required: true
        schema:
          type: integer
          format: int64
      - name: userId
        in: path
        required: true
        schema:
          type: integer
          format: int64
    responses:
      '204':
        description: Participant removed
      '403':
        $ref: '#/components/responses/Forbidden'
      '404':
        $ref: '#/components/responses/NotFound'

/api/events/{eventId}/summary:
  get:
    summary: Get event response summary
    operationId: getEventSummary
    tags: [Events]
    security:
      - bearerAuth: []
    parameters:
      - name: eventId
        in: path
        required: true
        schema:
          type: integer
          format: int64
    responses:
      '200':
        description: Response summary
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventSummaryResponse'

/api/events/my-events:
  get:
    summary: Get events for current user
    operationId: getMyEvents
    tags: [Events]
    security:
      - bearerAuth: []
    parameters:
      - name: filter
        in: query
        description: Filter events
        schema:
          type: string
          enum: [invited, created, confirmed, tentative, declined, upcoming, past]
      - name: limit
        in: query
        schema:
          type: integer
          default: 20
      - name: offset
        in: query
        schema:
          type: integer
          default: 0
    responses:
      '200':
        description: User's events
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventSearchResponse'

# =====================================================
# SCHEMAS
# =====================================================

components:
  schemas:

    EventType:
      type: string
      enum:
        - NETS_SESSION
        - PRACTICE_MATCH
        - TOURNAMENT_MATCH

    EventStatus:
      type: string
      enum:
        - DRAFT
        - PUBLISHED
        - CANCELLED
        - COMPLETED

    EventVisibility:
      type: string
      enum:
        - PRIVATE
        - TEAM_ONLY
        - PUBLIC

    ResponseStatus:
      type: string
      enum:
        - INVITED
        - YES
        - NO
        - TENTATIVE

    CreateEventRequest:
      type: object
      required:
        - title
        - eventType
        - eventDate
        - location
        - city
        - visibility
      properties:
        title:
          type: string
          minLength: 5
          maxLength: 120
          example: "Sunday Practice Match - Cubbon Park"
        description:
          type: string
          maxLength: 2000
          example: "Regular weekend match. Bring your own kit."
        eventType:
          $ref: '#/components/schemas/EventType'
        eventDate:
          type: string
          format: date-time
          example: "2026-01-12T09:00:00+05:30"
        durationMinutes:
          type: integer
          minimum: 30
          maximum: 480
          example: 180
        location:
          type: string
          maxLength: 200
          example: "Cubbon Park Cricket Ground, Gate 3"
        city:
          type: string
          maxLength: 100
          example: "Bengaluru"
        visibility:
          $ref: '#/components/schemas/EventVisibility'
        teamId:
          type: integer
          format: int64
          description: "Optional team association"
        maxParticipants:
          type: integer
          minimum: 1
          maximum: 100
          example: 22
        minParticipants:
          type: integer
          minimum: 1
          example: 11
        autoAcceptRequests:
          type: boolean
          default: false
          description: "Auto-accept join requests for public events"

    UpdateEventRequest:
      type: object
      properties:
        title:
          type: string
          minLength: 5
          maxLength: 120
        description:
          type: string
          maxLength: 2000
        eventDate:
          type: string
          format: date-time
        durationMinutes:
          type: integer
          minimum: 30
          maximum: 480
        location:
          type: string
          maxLength: 200
        maxParticipants:
          type: integer
          minimum: 1
        minParticipants:
          type: integer
          minimum: 1
        autoAcceptRequests:
          type: boolean

    EventResponse:
      type: object
      required:
        - id
        - title
        - eventType
        - eventStatus
        - eventDate
        - location
        - city
        - visibility
        - createdBy
        - createdAt
      properties:
        id:
          type: integer
          format: int64
        title:
          type: string
        description:
          type: string
        eventType:
          $ref: '#/components/schemas/EventType'
        eventStatus:
          $ref: '#/components/schemas/EventStatus'
        eventDate:
          type: string
          format: date-time
        durationMinutes:
          type: integer
        location:
          type: string
        city:
          type: string
        visibility:
          $ref: '#/components/schemas/EventVisibility'
        createdBy:
          $ref: '#/components/schemas/UserSummary'
        teamId:
          type: integer
          format: int64
        teamName:
          type: string
        maxParticipants:
          type: integer
        minParticipants:
          type: integer
        autoAcceptRequests:
          type: boolean
        isActive:
          type: boolean
        createdAt:
          type: string
          format: date-time
        updatedAt:
          type: string
          format: date-time
        cancelledAt:
          type: string
          format: date-time
        cancellationReason:
          type: string

    EventDetailResponse:
      allOf:
        - $ref: '#/components/schemas/EventResponse'
        - type: object
          properties:
            participantSummary:
              $ref: '#/components/schemas/ParticipantSummary'
            myResponse:
              $ref: '#/components/schemas/ResponseStatus'
              description: "Current user's response status (if invited)"
            canEdit:
              type: boolean
              description: "Can current user edit this event"
            canCancel:
              type: boolean
              description: "Can current user cancel this event"

    EventSearchResponse:
      type: object
      required:
        - events
        - total
        - limit
        - offset
      properties:
        events:
          type: array
          items:
            $ref: '#/components/schemas/EventSummary'
        total:
          type: integer
          format: int64
        limit:
          type: integer
        offset:
          type: integer

    EventSummary:
      type: object
      required:
        - id
        - title
        - eventType
        - eventDate
        - location
        - city
      properties:
        id:
          type: integer
          format: int64
        title:
          type: string
        eventType:
          $ref: '#/components/schemas/EventType'
        eventStatus:
          $ref: '#/components/schemas/EventStatus'
        eventDate:
          type: string
          format: date-time
        location:
          type: string
        city:
          type: string
        visibility:
          $ref: '#/components/schemas/EventVisibility'
        createdBy:
          $ref: '#/components/schemas/UserSummary'
        teamName:
          type: string
        confirmedCount:
          type: integer
          description: "Number of confirmed participants"
        totalInvited:
          type: integer
          description: "Total number of invited participants"
        maxParticipants:
          type: integer
        myResponse:
          $ref: '#/components/schemas/ResponseStatus'
          description: "Current user's response (if invited)"

    InviteParticipantsRequest:
      type: object
      required:
        - userIds
      properties:
        userIds:
          type: array
          items:
            type: integer
            format: int64
          minItems: 1
          maxItems: 100

    InviteParticipantsResponse:
      type: object
      required:
        - successCount
        - failureCount
      properties:
        successCount:
          type: integer
        failureCount:
          type: integer
        successIds:
          type: array
          items:
            type: integer
            format: int64
        failures:
          type: array
          items:
            type: object
            properties:
              userId:
                type: integer
                format: int64
              reason:
                type: string

    EventResponseRequest:
      type: object
      required:
        - responseStatus
      properties:
        responseStatus:
          $ref: '#/components/schemas/ResponseStatus'
        comment:
          type: string
          maxLength: 500

    ParticipantListResponse:
      type: object
      required:
        - participants
        - summary
      properties:
        participants:
          type: array
          items:
            $ref: '#/components/schemas/ParticipantDetail'
        summary:
          $ref: '#/components/schemas/ParticipantSummary'

    ParticipantDetail:
      type: object
      required:
        - userId
        - userName
        - responseStatus
        - invitedAt
      properties:
        userId:
          type: integer
          format: int64
        userName:
          type: string
        fullName:
          type: string
        profilePhotoUrl:
          type: string
        responseStatus:
          $ref: '#/components/schemas/ResponseStatus'
        responseComment:
          type: string
        invitedAt:
          type: string
          format: date-time
        respondedAt:
          type: string
          format: date-time

    ParticipantSummary:
      type: object
      required:
        - totalInvited
        - confirmed
        - declined
        - tentative
        - noResponse
      properties:
        totalInvited:
          type: integer
        confirmed:
          type: integer
          description: "Count of YES responses"
        declined:
          type: integer
          description: "Count of NO responses"
        tentative:
          type: integer
          description: "Count of TENTATIVE responses"
        noResponse:
          type: integer
          description: "Count of INVITED (not responded)"
        minimumMet:
          type: boolean
          description: "Has minimum participant threshold been met?"

    EventSummaryResponse:
      type: object
      required:
        - eventId
        - summary
        - confirmedPlayers
        - tentativePlayers
      properties:
        eventId:
          type: integer
          format: int64
        summary:
          $ref: '#/components/schemas/ParticipantSummary'
        confirmedPlayers:
          type: array
          items:
            $ref: '#/components/schemas/ParticipantDetail'
        tentativePlayers:
          type: array
          items:
            $ref: '#/components/schemas/ParticipantDetail'
        declinedPlayers:
          type: array
          items:
            $ref: '#/components/schemas/ParticipantDetail'
        noResponsePlayers:
          type: array
          items:
            $ref: '#/components/schemas/ParticipantDetail'

    UserSummary:
      type: object
      required:
        - id
        - name
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
        email:
          type: string
          format: email
```

### 8.2 API Endpoint Summary

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/events` | Create new event | Yes |
| GET | `/api/events` | Search/list events | No |
| GET | `/api/events/{eventId}` | Get event details | No (public events) |
| PUT | `/api/events/{eventId}` | Update event | Yes (creator only) |
| DELETE | `/api/events/{eventId}` | Cancel event | Yes (creator only) |
| POST | `/api/events/{eventId}/participants` | Invite participants | Yes (creator/admin) |
| GET | `/api/events/{eventId}/participants` | Get participant list | Yes |
| DELETE | `/api/events/{eventId}/participants/{userId}` | Remove participant | Yes (creator/admin) |
| POST | `/api/events/{eventId}/respond` | RSVP to event | Yes |
| GET | `/api/events/{eventId}/summary` | Get response summary | Yes (creator/admin) |
| GET | `/api/events/my-events` | Get my events | Yes |

---

## 9. Security & Authorization

### 9.1 Authentication

Following existing JWT-based stateless authentication:

- All protected endpoints require `Authorization: Bearer <token>` header
- JWT validated by `JwtAuthenticationFilter`
- User identity extracted via `CurrentUser.getUserId()`
- No database hit per request (user info in JWT claims)

### 9.2 Authorization Matrix

| Action | Role/Permission Required | Implementation |
|--------|-------------------------|----------------|
| **Create Event** | Authenticated user | Any logged-in user |
| **Create Team Event** | Team ADMIN or COORDINATOR | Check `TeamMemberRepository.findByTeamIdAndUserId()` |
| **View Public Event** | Anyone (no auth) | Public access |
| **View Private Event** | Event creator or invitee | Check `created_by_user_id` or `event_participant.user_id` |
| **Edit Event** | Event creator only | Check `created_by_user_id == currentUserId` |
| **Cancel Event** | Event creator only | Check `created_by_user_id == currentUserId` |
| **Invite Participants** | Event creator or team admin | Check event ownership or team role |
| **Remove Participant** | Event creator or team admin | Check event ownership or team role |
| **RSVP to Event** | Invited participant | Check `event_participant.user_id == currentUserId` |
| **View Participant List** | Event creator or team member | Check event ownership or team membership |
| **View Response Summary** | Event creator or team admin | Check event ownership or team role |

### 9.3 Authorization Implementation Pattern

```java
// EventController.java

@RequireAuthentication
public ResponseEntity<EventResponse> _updateEvent(Long eventId, UpdateEventRequest request) {
    Long currentUserId = CurrentUser.getUserId();

    // Authorization check
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new EventException(EventError.EVENT_NOT_FOUND));

    if (!event.getCreatedByUserId().equals(currentUserId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Proceed with update
    EventResponse response = eventService.updateEvent(eventId, request, currentUserId);
    return ResponseEntity.ok(response);
}
```

### 9.4 Security Considerations

#### Data Access Control

1. **Event Visibility**:
   - **PRIVATE**: Only creator and invited participants can view
   - **TEAM_ONLY**: Team members can view
   - **PUBLIC**: Anyone can view (for discovery)

2. **Participant Data**:
   - Full participant list visible to event creator and team admins
   - Other participants see summary counts only
   - Personal response comments private (only visible to creator)

#### Input Validation

```java
// Validation rules in CreateEventRequest

@NotBlank(message = "Title is required")
@Size(min = 5, max = 120, message = "Title must be between 5 and 120 characters")
private String title;

@NotNull(message = "Event date is required")
@Future(message = "Event date must be in the future")
private OffsetDateTime eventDate;

@Min(value = 1, message = "Max participants must be at least 1")
@Max(value = 100, message = "Max participants cannot exceed 100")
private Integer maxParticipants;
```

#### Rate Limiting

Apply rate limiting to prevent abuse:

```java
// EventController.java

@PostMapping("/api/events")
@RateLimited(requests = 10, windowSeconds = 3600) // 10 events per hour
public ResponseEntity<EventResponse> createEvent(...) { ... }

@PostMapping("/api/events/{eventId}/participants")
@RateLimited(requests = 20, windowSeconds = 300) // 20 invite operations per 5 min
public ResponseEntity<InviteParticipantsResponse> inviteParticipants(...) { ... }
```

#### SQL Injection Prevention

- Use JPA/Hibernate with parameterized queries
- Never concatenate user input in JPQL queries
- Use `@Param` annotation for named parameters

```java
// Safe query example
@Query("SELECT e FROM Event e WHERE " +
       "(:city IS NULL OR LOWER(e.city) = LOWER(:city)) AND " +
       "(:eventType IS NULL OR e.eventType = :eventType)")
Page<Event> findByFilters(@Param("city") String city,
                          @Param("eventType") EventType eventType,
                          Pageable pageable);
```

#### CSRF Protection

- Not required for stateless JWT APIs
- `SameSite=Strict` cookies already configured
- All state-changing operations require authentication

---

## 10. Edge Cases & Business Rules

### 10.1 Event Creation Edge Cases

| Scenario | Business Rule | Implementation |
|----------|--------------|----------------|
| **Event date in past** | Reject with validation error | `@Future` validation + custom check for minimum 2 hours ahead |
| **Team-linked event but user not admin** | Reject with 403 Forbidden | Check `TeamMemberRepository` for user role |
| **Duplicate event title** | Allow (warn user) | No unique constraint, but show warning in UI |
| **Max participants < Min participants** | Reject with validation error | Custom validation: `@AssertTrue` method |
| **Team deleted after event created** | Event becomes standalone | Foreign key `ON DELETE SET NULL` |

### 10.2 RSVP Edge Cases

| Scenario | Business Rule | Implementation |
|----------|--------------|----------------|
| **User not invited** | Reject with 403 Forbidden | Check `event_participant` table |
| **Event already started** | Reject with 400 Bad Request | Check `event_date < now()` |
| **Event cancelled** | Reject with 400 Bad Request | Check `event_status = 'CANCELLED'` |
| **Changing YES to NO last minute** | Allow but notify organizer | Update response + trigger notification |
| **Event full, user responds YES** | Add to waiting list (future) | For MVP: Reject with "Event full" |
| **User responds YES twice** | Update existing response | Use `ON CONFLICT UPDATE` or check existence first |

### 10.3 Participant Management Edge Cases

| Scenario | Business Rule | Implementation |
|----------|--------------|----------------|
| **Invite user already invited** | Silently ignore (idempotent) | Check existence, skip if exists |
| **Invite non-existent user** | Return in failure list | Validate user existence, add to failures |
| **Remove participant who responded YES** | Allow, notify participant | Soft delete + notification |
| **Invite user not in team (for team event)** | Allow for PUBLIC/PRIVATE, reject for TEAM_ONLY | Validate based on visibility |
| **Bulk invite >100 users** | Reject with validation error | Max array size validation |

### 10.4 Event Modification Edge Cases

| Scenario | Business Rule | Implementation |
|----------|--------------|----------------|
| **Edit event <2 hours before start** | Reject with 400 Bad Request | Check `event_date - now() < 2 hours` |
| **Change date, some participants responded** | Allow, notify all participants | Update event + bulk notification |
| **Reduce max participants below current confirmed** | Reject with 409 Conflict | Check `confirmed_count <= new_max` |
| **Delete event with 20 confirmed participants** | Allow (soft delete), notify all | Set `cancelled` status + bulk notification |
| **Un-cancel cancelled event** | Not allowed | Must create new event |

### 10.5 Notification Edge Cases

| Scenario | Business Rule | Implementation |
|----------|--------------|----------------|
| **User disabled notifications** | Skip notification | Check user notification preferences |
| **Email service down** | Queue for retry | Use message queue (RabbitMQ/SQS) |
| **Notification send fails** | Log error, continue | Don't block main operation |
| **Duplicate notifications** | Deduplicate within 5 minutes | Track last notification timestamp |

### 10.6 Concurrency Scenarios

| Scenario | Business Rule | Implementation |
|----------|--------------|----------------|
| **Two users respond simultaneously** | Both succeed (last write wins) | Use database transaction isolation |
| **Organizer deletes event while user responds** | Response fails with 404 | Foreign key constraint + error handling |
| **Event reaches max capacity simultaneously** | First N succeed, rest fail/waitlisted | Use optimistic locking or database constraint |

---

## 11. Non-Functional Requirements

### 11.1 Performance Requirements

| Metric | Target | Measurement |
|--------|--------|-------------|
| **API Response Time** | <200ms (p95) | Event search, RSVP operations |
| **Event Creation** | <500ms (p95) | Including invitations |
| **Participant List Load** | <300ms (p95) | For events with 100 participants |
| **Concurrent Users** | 1,000+ | Peak load during match season |
| **Database Query Time** | <100ms (p95) | All event queries |

### 11.2 Scalability

- **Horizontal Scaling**: Stateless API design allows multiple instances
- **Database Connection Pooling**: HikariCP with 10-20 connections per instance
- **Caching Strategy**:
  - Cache event details in Redis (TTL: 5 minutes)
  - Cache participant summaries (TTL: 2 minutes)
  - Invalidate cache on event updates

```java
// Caching example
@Cacheable(value = "event-details", key = "#eventId", unless = "#result == null")
public EventDetailResponse getEventDetails(Long eventId) { ... }

@CacheEvict(value = "event-details", key = "#eventId")
public void updateEvent(Long eventId, UpdateEventRequest request) { ... }
```

### 11.3 Availability

- **Target**: 99.5% uptime
- **Graceful Degradation**:
  - If Redis down, skip caching (slower but functional)
  - If notification service down, queue notifications for retry
- **Health Checks**: Existing `/actuator/health` endpoint

### 11.4 Data Integrity

- **Transaction Management**: `@Transactional` for all write operations
- **Isolation Level**: `READ_COMMITTED` (default PostgreSQL)
- **Foreign Key Constraints**: Enforce referential integrity
- **Validation**: Bean validation + custom business rule validation

### 11.5 Monitoring & Observability

```java
// Logging pattern
@Slf4j
@Service
public class EventService {

    public EventResponse createEvent(CreateEventRequest request, Long userId) {
        log.info("Creating event: title={}, type={}, userId={}",
                 request.getTitle(), request.getEventType(), userId);

        try {
            Event event = buildEvent(request, userId);
            Event savedEvent = eventRepository.save(event);

            log.info("Event created successfully: eventId={}, title={}",
                     savedEvent.getId(), savedEvent.getTitle());

            return mapToResponse(savedEvent);
        } catch (Exception e) {
            log.error("Failed to create event: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
```

**Metrics to Track**:
- Event creation rate (events/hour)
- RSVP response rate (% of invites responded within 24 hours)
- Event cancellation rate
- Average participants per event
- No-show rate (confirmed vs actual attendance)

### 11.6 Security & Compliance

- **Data Privacy**: No PII in logs (mask emails, phone numbers)
- **GDPR Compliance**:
  - User can delete account (cascade delete participations)
  - Export user's event history
- **Audit Logging**: Track event modifications (who changed what when)
- **Encryption**:
  - Data in transit: TLS 1.3
  - Data at rest: PostgreSQL transparent encryption (infrastructure level)

---

## 12. Future Enhancements

### 12.1 Phase 2 Features (Q2 2026)

1. **Recurring Events**
   - Weekly nets sessions
   - Monthly tournaments
   - Cron-based event generation

2. **Waiting List Management**
   - Auto-promote from waiting list when participant cancels
   - Notify waiting list users

3. **Advanced Notifications**
   - WhatsApp notifications (via Twilio)
   - SMS reminders
   - Email digests (weekly schedule)

4. **Event Analytics**
   - Captain dashboard with statistics
   - Player reliability scores
   - Attendance trends

5. **Calendar Integration**
   - Export to Google Calendar
   - iCal format support
   - Sync with external calendars

### 12.2 Phase 3 Features (Q3 2026)

1. **Payment Integration**
   - Collect match fees
   - Split costs (ground booking, equipment)
   - Payment tracking

2. **Smart Scheduling**
   - AI-powered best time suggestions
   - Weather-based recommendations
   - Conflict detection with other events

3. **Team Selection**
   - Auto-suggest playing XI based on availability
   - Role-based team composition
   - Player skill ratings

4. **Match Management**
   - Scorekeeping integration
   - Live match updates
   - Post-match statistics

### 12.3 Premium Features (Monetization)

1. **Advanced Analytics** ($5/month)
   - Player performance trends
   - Team analytics dashboard
   - Custom reports

2. **WhatsApp Notifications** (Pay-per-use)
   - $0.10 per notification
   - Bulk pricing for teams

3. **Event Sponsorship**
   - Allow local businesses to sponsor events
   - Revenue sharing model

---

## 13. Implementation Roadmap

### 13.1 Sprint 1 (Week 1-2): Foundation

**Goal**: Core database schema and entity models

**Tasks**:
- [ ] Create database migration scripts (Flyway/Liquibase)
- [ ] Implement `Event` entity with all fields
- [ ] Implement `EventParticipant` entity
- [ ] Create repositories with basic CRUD
- [ ] Write unit tests for entities
- [ ] Set up test data fixtures

**Deliverables**:
- Database schema deployed
- Entity models with tests
- Basic repository tests passing

---

### 13.2 Sprint 2 (Week 3-4): Event Management API

**Goal**: Event CRUD operations

**Tasks**:
- [ ] Define OpenAPI spec for event endpoints
- [ ] Generate DTOs from OpenAPI spec
- [ ] Implement `EventService` (create, update, cancel, search)
- [ ] Implement `EventController` with authorization
- [ ] Add custom error handling (`EventError`, `EventException`)
- [ ] Write integration tests for event API
- [ ] Update Postman collection

**Deliverables**:
- Working event creation API
- Event search/filter API
- Event update/cancel API
- API tests passing
- Postman collection updated

---

### 13.3 Sprint 3 (Week 5-6): RSVP System

**Goal**: Participant invitation and response system

**Tasks**:
- [ ] Implement `EventParticipantService`
- [ ] API: Invite participants (bulk)
- [ ] API: RSVP to event (YES/NO/TENTATIVE)
- [ ] API: Get participant list
- [ ] API: Get response summary
- [ ] Business rules (event full, duplicate invites)
- [ ] Integration tests for RSVP flow
- [ ] Update Postman collection

**Deliverables**:
- Working invitation system
- RSVP functionality
- Participant list API
- Summary statistics API

---

### 13.4 Sprint 4 (Week 7-8): Captain Dashboard & Notifications

**Goal**: Real-time visibility and notifications

**Tasks**:
- [ ] API: Get my events (created/invited)
- [ ] API: Event response summary with participant details
- [ ] Implement notification service integration
- [ ] Notification: Event invitation
- [ ] Notification: RSVP response to organizer
- [ ] Notification: Event reminder (24 hours before)
- [ ] Notification: Event cancelled
- [ ] Notification: Minimum threshold met
- [ ] Integration tests for notifications
- [ ] Update Postman collection

**Deliverables**:
- Captain dashboard API
- Notification system integrated
- All notification types working
- End-to-end tests passing

---

### 13.5 Sprint 5 (Week 9-10): Event Discovery & Polish

**Goal**: Public event discovery and production readiness

**Tasks**:
- [ ] API: Discover public events
- [ ] API: Filter by city, type, date
- [ ] Implement caching (Redis)
- [ ] Performance optimization (database indexes, query tuning)
- [ ] Security audit (authorization checks, input validation)
- [ ] API documentation (Swagger UI)
- [ ] Load testing (JMeter/Gatling)
- [ ] Bug fixes and edge case handling

**Deliverables**:
- Event discovery API
- Performance optimizations
- Production-ready code
- Load test results
- Security audit completed

---

### 13.6 Sprint 6 (Week 11-12): Testing & Deployment

**Goal**: Comprehensive testing and production deployment

**Tasks**:
- [ ] End-to-end testing (all user flows)
- [ ] Edge case testing (concurrency, errors)
- [ ] User acceptance testing (UAT)
- [ ] Database migration scripts for production
- [ ] Production deployment runbook
- [ ] Monitoring and alerting setup
- [ ] Deploy to staging environment
- [ ] Deploy to production
- [ ] Post-deployment verification

**Deliverables**:
- 90%+ test coverage
- UAT sign-off
- Production deployment completed
- Monitoring dashboards live

---

## 14. Appendices

### 14.1 Glossary

| Term | Definition |
|------|------------|
| **Event** | A cricket activity (nets session, practice match, tournament match) |
| **Organizer** | User who creates an event (typically team captain) |
| **Participant** | User invited to an event |
| **RSVP** | Participant's response to event invitation (YES/NO/TENTATIVE) |
| **Soft Delete** | Marking record as inactive rather than deleting from database |
| **Visibility** | Access control level for event (PRIVATE/TEAM_ONLY/PUBLIC) |
| **Minimum Threshold** | Minimum number of confirmed players required for event |
| **Waiting List** | Queue of users wanting to join a full event |

### 14.2 References

1. **Existing Codebase Analysis** - [Codebase Architecture Summary](#6-system-design)
2. **Postman Collection** - `/PlayMatch.postman_collection.json`
3. **OpenAPI Spec** - `/src/main/resources/api/api-docs.yaml`
4. **Database Schema** - PostgreSQL 16 (existing schema in `app_user`, `team`, `player_profile`)

### 14.3 Open Questions

1. **Payment Integration**: Which payment gateway to use? (Razorpay, Stripe, PayPal)
2. **Notification Preferences**: Should users be able to opt-out of specific notification types?
3. **Event Approval Workflow**: Should team admins approve events created by coordinators?
4. **Player Ratings**: Should players rate each other post-event? Privacy implications?
5. **WhatsApp Integration**: Use official WhatsApp Business API or third-party service?

### 14.4 Success Criteria

**MVP Success Metrics** (3 months post-launch):

✅ **Adoption**:
- 500+ events created
- 1,000+ active users using event feature
- 70%+ of teams using event management

✅ **Engagement**:
- 80%+ RSVP response rate within 24 hours
- 60%+ events reaching minimum participant threshold
- <15% event cancellation rate

✅ **Satisfaction**:
- NPS > 40
- <5% bug reports per active user
- Positive feedback from 70%+ users

✅ **Technical**:
- 99%+ uptime
- <300ms API response time (p95)
- Zero data loss incidents

---

## Document Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-09 | Product Team | Initial draft based on current codebase analysis |

---

**Next Steps**:

1. Review this PRD with stakeholders
2. Validate assumptions with user interviews
3. Finalize MVP scope
4. Begin Sprint 1 implementation

**Questions or Feedback**: Contact product team or open GitHub issue.
