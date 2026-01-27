# GameTeam MVP - Frontend API Specification

**Version:** 1.0
**Last Updated:** January 23, 2026
**Base URL:** `http://localhost:8080` (Development) | `https://api.gameteam.com` (Production)

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
4. [Data Models](#data-models)
5. [Error Handling](#error-handling)
6. [User Flows](#user-flows)
7. [State Management](#state-management)
8. [Edge Cases & Validations](#edge-cases--validations)

---

## Overview

### Architecture
- **Protocol:** REST API over HTTPS
- **Data Format:** JSON
- **Authentication:** JWT Bearer Token (phone-based OTP)
- **Timezone:** All timestamps in ISO 8601 format with timezone offset (e.g., `2026-02-01T15:00:00+05:30`)

### Key Concepts

**Match-Centric System:**
- Users don't have global roles
- Roles are derived per match based on behavior
- Captain = user who creates the match
- Player = user who responds YES
- Emergency Player = approved emergency request

**Silence = Not Available:**
- If a user doesn't respond, they're considered unavailable
- No automatic reminders or nagging

**Two Identity Systems:**
1. **Regular Users** - Email-based (existing system)
2. **MVP Users** - Phone-based (new system, isolated)

---

## Authentication

### Authentication Flow

```
┌─────────────┐
│ User enters │
│ phone number│
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ POST /otp/request│  ← No auth required
└──────┬──────────┘
       │
       │ (OTP sent to phone - currently hardcoded "123456")
       │
       ▼
┌──────────────────┐
│ User enters OTP  │
└──────┬───────────┘
       │
       ▼
┌─────────────────┐
│ POST /otp/verify │  ← No auth required
└──────┬──────────┘
       │
       │ Returns: { accessToken, refreshToken, requiresProfile }
       │
       ▼
   ┌───────┐
   │ Store │
   │tokens │
   └───┬───┘
       │
       ▼
┌──────────────────┐
│ requiresProfile? │
└──────┬───────────┘
       │
   YES │           NO
       │            │
       ▼            ▼
┌─────────────┐  ┌──────────┐
│POST /profile│  │Proceed to│
│ (set name)  │  │   app    │
└─────────────┘  └──────────┘
```

### Endpoints

#### 1. Request OTP

**Endpoint:** `POST /v2/mvp/auth/otp/request`
**Auth Required:** No
**Rate Limit:** 3 requests per 10 minutes per phone number

**Request:**
```json
{
  "phoneNumber": "+919876543210"
}
```

**Validation Rules:**
- Phone number must start with `+`
- Must contain 10-15 digits after `+`
- Pattern: `^\\+\\d{10,15}$`

**Success Response:** `204 No Content`

**Error Responses:**
```json
// 400 Bad Request - Invalid phone format
{
  "code": "MVP-AUTH-006",
  "title": "Invalid phone number format",
  "status": 400,
  "detail": "Phone number must start with + and contain 10-15 digits"
}

// 429 Too Many Requests - Rate limit exceeded
{
  "code": "MVP-AUTH-004",
  "title": "OTP rate limit exceeded",
  "status": 429,
  "detail": "Maximum 3 OTP requests per 10 minutes. Please wait."
}
```

**Frontend Notes:**
- Show loading indicator while requesting OTP
- Display countdown timer (10 minutes) before allowing retry
- Current MVP: OTP is hardcoded as "123456" - check browser console logs
- Production: OTP will be sent via SMS

---

#### 2. Verify OTP

**Endpoint:** `POST /v2/mvp/auth/otp/verify`
**Auth Required:** No
**Rate Limit:** 5 attempts per OTP

**Request:**
```json
{
  "phoneNumber": "+919876543210",
  "otpCode": "123456"
}
```

**Validation Rules:**
- OTP must be exactly 6 digits
- Pattern: `^\\d{6}$`
- Phone number must match the one used in `/otp/request`

**Success Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "phoneNumber": "+919876543210",
  "name": "Rahul Sharma",
  "requiresProfile": false
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `accessToken` | string | JWT token for API authentication (1 hour expiry) |
| `refreshToken` | string | Token for getting new access token (24 hour expiry) |
| `userId` | number | Unique user identifier |
| `phoneNumber` | string | User's phone number |
| `name` | string\|null | User's name (null if not set) |
| `requiresProfile` | boolean | `true` if user needs to set name |

**Error Responses:**
```json
// 400 Bad Request - Invalid OTP
{
  "code": "MVP-AUTH-001",
  "title": "Invalid OTP code",
  "status": 400,
  "detail": "The OTP code entered is incorrect. 3 attempts remaining."
}

// 400 Bad Request - OTP Expired
{
  "code": "MVP-AUTH-002",
  "title": "OTP has expired",
  "status": 400,
  "detail": "This OTP has expired. Please request a new one."
}

// 429 Too Many Requests - Max attempts
{
  "code": "MVP-AUTH-003",
  "title": "Maximum OTP attempts exceeded",
  "status": 429,
  "detail": "You have exceeded the maximum number of attempts. Please request a new OTP."
}
```

**Frontend Notes:**
- Store `accessToken` in memory or secure storage (NOT localStorage for production)
- Store `refreshToken` in httpOnly cookie (if backend supports) or secure storage
- If `requiresProfile === true`, redirect to profile setup screen
- Show remaining attempts on error
- Auto-navigate to app home after successful verification

---

#### 3. Update Profile

**Endpoint:** `POST /v2/mvp/auth/profile`
**Auth Required:** Yes (Bearer token)

**Request:**
```json
{
  "name": "Rahul Sharma",
  "area": "Koramangala"
}
```

**Validation Rules:**
| Field | Required | Min | Max | Description |
|-------|----------|-----|-----|-------------|
| `name` | Yes | 2 | 120 | User's display name |
| `area` | No | 0 | 100 | Location/area (for emergency pool) |

**Success Response:** `204 No Content`

**Error Responses:**
```json
// 400 Bad Request - Validation error
{
  "code": "VALIDATION_ERROR",
  "title": "Validation failed",
  "status": 400,
  "detail": "Name must be between 2 and 120 characters"
}

// 401 Unauthorized - No/invalid token
{
  "code": "MVP-AUTH-011",
  "title": "Unauthorized access",
  "status": 401,
  "detail": "Valid authentication token required"
}
```

**Frontend Notes:**
- Required step for new users
- Name is mandatory, area is optional
- After successful update, proceed to main app
- Consider pre-filling area based on location permissions

---

## API Endpoints

### Match Management

#### 1. Create Match

**Endpoint:** `POST /v2/mvp/matches`
**Auth Required:** Yes (Creator becomes captain)

**Request:**
```json
{
  "teamName": "Koramangala Knights",
  "eventType": "PRACTICE",
  "ballCategory": "LEATHER",
  "ballVariant": "WHITE",
  "groundMapsUrl": "https://maps.google.com/?q=@12.9352,77.6245",
  "overs": 20,
  "feePerPerson": 200,
  "emergencyFee": 300,
  "requiredPlayers": 11,
  "backupSlots": 2,
  "emergencyEnabled": true,
  "startTime": "2026-02-01T15:00:00+05:30"
}
```

**Field Specifications:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `teamName` | string | Yes | 2-100 chars | Team/group name |
| `eventType` | enum | Yes | PRACTICE\|TOURNAMENT\|NETS | Type of match |
| `ballCategory` | enum | Yes | LEATHER\|TENNIS | Ball type |
| `ballVariant` | enum | Yes | WHITE\|RED\|PINK (leather)<br>HARD\|SOFT (tennis) | Ball variant |
| `groundMapsUrl` | string | Yes | Valid URL | Google Maps link |
| `overs` | number | Yes | 1-50 | Number of overs |
| `feePerPerson` | number | Yes | >= 0 | Fee per participant (₹) |
| `emergencyFee` | number | No | >= 0 | Fee for emergency players (₹) |
| `requiredPlayers` | number | No | >= 1 | Main team size (default: 11) |
| `backupSlots` | number | No | >= 0 | Backup player slots (default: 2) |
| `emergencyEnabled` | boolean | No | - | Enable emergency requests (default: false) |
| `startTime` | string | Yes | ISO 8601, future date | Match start time |

**Success Response:** `200 OK`
```json
{
  "matchId": "550e8400-e29b-41d4-a716-446655440000",
  "teamInviteUrl": "http://localhost:8080/v2/mvp/invites/ABC12345",
  "emergencyInviteUrl": "http://localhost:8080/v2/mvp/invites/XYZ67890"
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `matchId` | UUID | Unique match identifier |
| `teamInviteUrl` | string | Shareable link for team members |
| `emergencyInviteUrl` | string\|null | Emergency invite link (null if not enabled) |

**Error Responses:**
```json
// 400 Bad Request - Validation error
{
  "code": "VALIDATION_ERROR",
  "title": "Validation failed",
  "status": 400,
  "detail": "Start time must be in the future"
}

// 401 Unauthorized
{
  "code": "MVP-AUTH-011",
  "title": "Unauthorized access",
  "status": 401,
  "detail": "Valid authentication token required"
}
```

**Frontend Notes:**
- Show date/time picker for `startTime` with timezone support
- Validate ball variant matches ball category client-side
- Parse Google Maps URL to extract lat/lng for map preview
- Display generated invite links immediately after creation
- Provide share buttons for WhatsApp, SMS, etc.
- Copy-to-clipboard functionality for invite URLs

---

#### 2. Get Match Details

**Endpoint:** `GET /v2/mvp/matches/{matchId}`
**Auth Required:** Optional (affects response detail)

**Path Parameters:**
- `matchId` (UUID) - Match identifier

**Success Response (Captain View):** `200 OK`
```json
{
  "matchId": "550e8400-e29b-41d4-a716-446655440000",
  "teamName": "Koramangala Knights",
  "eventType": "PRACTICE",
  "ballCategory": "LEATHER",
  "ballVariant": "WHITE",
  "groundMapsUrl": "https://maps.google.com/?q=@12.9352,77.6245",
  "groundLat": 12.9352,
  "groundLng": 77.6245,
  "overs": 20,
  "feePerPerson": 200,
  "emergencyFee": 300,
  "requiredPlayers": 11,
  "backupSlots": 2,
  "emergencyEnabled": true,
  "status": "CREATED",
  "startTime": "2026-02-01T15:00:00+05:30",
  "createdAt": "2026-01-23T10:30:00+05:30",
  "captainId": 1,
  "captainName": "Rahul Sharma",
  "captainPhone": "+919876543210",
  "teamCount": 8,
  "backupCount": 2,
  "emergencyCount": 1,
  "participants": [
    {
      "userId": 2,
      "name": "Virat Kumar",
      "phoneNumber": "+919876543211",
      "role": "TEAM",
      "status": "CONFIRMED",
      "feeAmount": 200,
      "paymentStatus": "UNPAID",
      "paymentMode": null
    },
    {
      "userId": 3,
      "name": "Rohit Patel",
      "phoneNumber": "+919876543212",
      "role": "EMERGENCY",
      "status": "CONFIRMED",
      "feeAmount": 300,
      "paymentStatus": "PAID",
      "paymentMode": "UPI"
    }
  ]
}
```

**Success Response (Non-Captain/Public View):** `200 OK`
```json
{
  "matchId": "550e8400-e29b-41d4-a716-446655440000",
  "teamName": "Koramangala Knights",
  "eventType": "PRACTICE",
  "ballCategory": "LEATHER",
  "ballVariant": "WHITE",
  "groundMapsUrl": "https://maps.google.com/?q=@12.9352,77.6245",
  "groundLat": 12.9352,
  "groundLng": 77.6245,
  "overs": 20,
  "feePerPerson": 200,
  "emergencyFee": 300,
  "requiredPlayers": 11,
  "backupSlots": 2,
  "emergencyEnabled": true,
  "status": "CREATED",
  "startTime": "2026-02-01T15:00:00+05:30",
  "createdAt": "2026-01-23T10:30:00+05:30",
  "captainId": null,
  "captainName": null,
  "captainPhone": null,
  "teamCount": 8,
  "backupCount": 2,
  "emergencyCount": 1,
  "participants": null
}
```

**Response Differences by Role:**

| Field | Captain View | Player/Public View |
|-------|--------------|-------------------|
| Basic match details | ✅ Full | ✅ Full |
| `captainId`, `captainName`, `captainPhone` | ✅ Visible | ❌ `null` |
| `teamCount`, `backupCount`, `emergencyCount` | ✅ Visible | ✅ Visible |
| `participants` (full list) | ✅ Full array | ❌ `null` |

**Participant Object:**
```json
{
  "userId": 2,
  "name": "Virat Kumar",
  "phoneNumber": "+919876543211",
  "role": "TEAM",
  "status": "CONFIRMED",
  "feeAmount": 200,
  "paymentStatus": "UNPAID",
  "paymentMode": null
}
```

**Enum Values:**

**Match Status:**
- `CREATED` - Just created, accepting responses
- `ACTIVE` - Match is ongoing
- `COMPLETED` - Match finished
- `CANCELLED` - Match cancelled

**Participant Role:**
- `TEAM` - Main team member
- `BACKUP` - Backup player
- `EMERGENCY` - Emergency player

**Participant Status:**
- `CONFIRMED` - Player confirmed
- `BACKED_OUT` - Player cancelled
- `NO_SHOW` - Player didn't show up

**Payment Status:**
- `PAID` - Payment received
- `UNPAID` - Payment pending

**Payment Mode:**
- `CASH` - Cash payment
- `UPI` - UPI payment
- `null` - Not paid yet

**Error Responses:**
```json
// 404 Not Found
{
  "code": "MVP-MATCH-001",
  "title": "Match not found",
  "status": 404,
  "detail": "No match found with the provided ID"
}
```

**Frontend Notes:**
- Check if current user is captain to show/hide admin actions
- Display participant counts as progress bars (e.g., "8/11 Team, 2/2 Backup")
- Show participant list only to captain
- Render Google Maps pin using `groundLat` and `groundLng`
- Display match status badge with appropriate color
- Implement auto-refresh for live participant count updates

---

#### 3. Respond to Match (YES/NO)

**Endpoint:** `POST /v2/mvp/matches/{matchId}/respond`
**Auth Required:** Yes

**Path Parameters:**
- `matchId` (UUID) - Match identifier

**Request:**
```json
{
  "response": "YES"
}
```

**Valid Responses:**
- `"YES"` - User wants to play
- `"NO"` - User cannot play

**Success Response:** `204 No Content`

**Error Responses:**
```json
// 400 Bad Request - Match full
{
  "code": "MVP-MATCH-002",
  "title": "Match is full",
  "status": 400,
  "detail": "All team and backup slots are filled"
}

// 400 Bad Request - Invalid status
{
  "code": "MVP-MATCH-005",
  "title": "Invalid match status for this operation",
  "status": 400,
  "detail": "Cannot respond to completed or cancelled matches"
}

// 404 Not Found
{
  "code": "MVP-MATCH-001",
  "title": "Match not found",
  "status": 404,
  "detail": "No match found with the provided ID"
}
```

**Frontend Notes:**
- **YES Response:**
  - User is automatically assigned a role (TEAM → BACKUP based on availability)
  - Fee amount is set based on role (emergency fee if emergency, otherwise regular fee)
  - Idempotent: Calling YES twice updates the same record

- **NO Response:**
  - User is marked as unavailable
  - Removes user from participants if previously confirmed
  - Also idempotent

- **UI Recommendations:**
  - Show "I'm In!" and "Can't Make It" buttons
  - Display current slot availability before response
  - Show assigned role after YES response
  - Confirm before NO if user previously said YES
  - Disable buttons after response (with option to change)

---

#### 4. Get My Games

**Endpoint:** `GET /v2/mvp/matches/my-games`
**Auth Required:** Yes

**No Parameters**

**Success Response:** `200 OK`
```json
{
  "games": [
    {
      "matchId": "550e8400-e29b-41d4-a716-446655440000",
      "teamName": "Koramangala Knights",
      "eventType": "PRACTICE",
      "ballCategory": "LEATHER",
      "ballVariant": "WHITE",
      "status": "CREATED",
      "startTime": "2026-02-01T15:00:00+05:30",
      "groundMapsUrl": "https://maps.google.com/?q=@12.9352,77.6245",
      "groundLat": 12.9352,
      "groundLng": 77.6245,
      "feePerPerson": 200,
      "emergencyFee": 300,
      "userRole": "CAPTAIN",
      "isCaptain": true,
      "teamCount": 8,
      "backupCount": 2,
      "emergencyCount": 1,
      "requiredPlayers": 11,
      "backupSlots": 2,
      "paymentStatus": null,
      "paymentMode": null,
      "feeAmount": null
    },
    {
      "matchId": "650e8400-e29b-41d4-a716-446655440001",
      "teamName": "Indiranagar Strikers",
      "eventType": "TOURNAMENT",
      "ballCategory": "TENNIS",
      "ballVariant": "HARD",
      "status": "COMPLETED",
      "startTime": "2026-01-20T14:00:00+05:30",
      "groundMapsUrl": "https://maps.google.com/?q=@12.9716,77.6412",
      "groundLat": 12.9716,
      "groundLng": 77.6412,
      "feePerPerson": 250,
      "emergencyFee": null,
      "userRole": "TEAM",
      "isCaptain": false,
      "teamCount": 11,
      "backupCount": 2,
      "emergencyCount": 0,
      "requiredPlayers": 11,
      "backupSlots": 2,
      "paymentStatus": "PAID",
      "paymentMode": "UPI",
      "feeAmount": 250
    }
  ],
  "totalCount": 2,
  "upcomingCount": 1,
  "completedCount": 1,
  "cancelledCount": 0
}
```

**Response Fields:**

**Top-Level:**
| Field | Type | Description |
|-------|------|-------------|
| `games` | array | List of matches where user is captain or participant |
| `totalCount` | number | Total number of matches |
| `upcomingCount` | number | Count of CREATED or ACTIVE matches |
| `completedCount` | number | Count of COMPLETED matches |
| `cancelledCount` | number | Count of CANCELLED matches |

**GameSummary Object:**
| Field | Type | Description |
|-------|------|-------------|
| `matchId` | UUID | Match identifier |
| `teamName` | string | Team name |
| `eventType` | enum | PRACTICE, TOURNAMENT, NETS |
| `ballCategory` | enum | LEATHER, TENNIS |
| `ballVariant` | enum | WHITE, RED, PINK, HARD, SOFT |
| `status` | enum | CREATED, ACTIVE, COMPLETED, CANCELLED |
| `startTime` | string | Match start time (ISO 8601) |
| `groundMapsUrl` | string | Google Maps link |
| `groundLat` | number\|null | Latitude (best-effort parsed) |
| `groundLng` | number\|null | Longitude (best-effort parsed) |
| `feePerPerson` | number | Regular fee amount |
| `emergencyFee` | number\|null | Emergency player fee (if enabled) |
| `userRole` | string | User's role: "CAPTAIN", "TEAM", "BACKUP", "EMERGENCY" |
| `isCaptain` | boolean | true if user is the captain |
| `teamCount` | number | Confirmed team participants |
| `backupCount` | number | Confirmed backup participants |
| `emergencyCount` | number | Confirmed emergency participants |
| `requiredPlayers` | number | Required team size |
| `backupSlots` | number | Backup slots available |
| `paymentStatus` | string\|null | User's payment status (PAID/UNPAID, null if captain) |
| `paymentMode` | string\|null | Payment method (CASH/UPI, null if not paid) |
| `feeAmount` | number\|null | User's fee amount (null if captain only) |

**Behavior:**
- Returns matches where user is either captain OR confirmed participant
- Sorted by startTime descending (most recent first)
- Includes all match statuses (CREATED, ACTIVE, COMPLETED, CANCELLED)
- Payment fields (paymentStatus, paymentMode, feeAmount) are null for captain-only matches

**Error Responses:**
```json
// 401 Unauthorized
{
  "code": "MVP-AUTH-011",
  "title": "Unauthorized access",
  "status": 401,
  "detail": "Valid authentication token required"
}
```

**Frontend Notes:**
- **Empty State:** Show friendly message if totalCount === 0
- **Grouping:** Consider grouping by status (Upcoming, Completed, Cancelled)
- **Sorting:** Matches pre-sorted by startTime DESC
- **Role Indicator:** Display user's role as badge/chip
- **Captain View:** Show "Your Match" badge if isCaptain === true
- **Payment Status:**
  - If captain: Don't show payment fields
  - If participant: Show payment status with color coding
- **Navigation:** Each game should link to detailed view (GET /matches/{id})
- **Refresh:** Implement pull-to-refresh on mobile
- **Caching:** Cache response and invalidate on match creation/response
- **Filters:** Allow filtering by status, role, date range
- **Stats Display:** Show upcomingCount/completedCount/cancelledCount as summary

**UI Examples:**

```typescript
// Display upcoming matches
const upcomingGames = response.games.filter(g =>
  g.status === 'CREATED' || g.status === 'ACTIVE'
);

// Display user's role
const getRoleBadge = (game: GameSummary) => {
  if (game.isCaptain) {
    return <Badge color="gold">Captain</Badge>;
  }
  return <Badge color="blue">{game.userRole}</Badge>;
};

// Display payment status
const getPaymentStatus = (game: GameSummary) => {
  if (game.isCaptain) return null; // Don't show for captains

  if (game.paymentStatus === 'PAID') {
    return <Status color="green">Paid via {game.paymentMode}</Status>;
  }
  return <Status color="red">Unpaid - ₹{game.feeAmount}</Status>;
};

// Group by status
const groupedGames = {
  upcoming: response.games.filter(g => g.status === 'CREATED' || g.status === 'ACTIVE'),
  completed: response.games.filter(g => g.status === 'COMPLETED'),
  cancelled: response.games.filter(g => g.status === 'CANCELLED')
};
```

---

#### 5. Complete Match (Captain Only)

**Endpoint:** `POST /v2/mvp/matches/{matchId}/complete`
**Auth Required:** Yes (Must be captain)

**Path Parameters:**
- `matchId` (UUID) - Match identifier

**Success Response:** `204 No Content`

**Side Effects:**
- Match status changes to `COMPLETED`
- Platform fee of ₹50 is recorded
- Emergency players may be added to emergency pool

**Error Responses:**
```json
// 403 Forbidden - Not captain
{
  "code": "MVP-AUTH-010",
  "title": "Only the match captain can perform this action",
  "status": 403,
  "detail": "You must be the match creator to complete this match"
}

// 400 Bad Request - Already completed
{
  "code": "MVP-MATCH-003",
  "title": "Match is already completed",
  "status": 400,
  "detail": "This match has already been marked as completed"
}
```

**Frontend Notes:**
- Show only to captain
- Display confirmation dialog before completing
- Show platform fee amount (₹50) in confirmation
- After completion, navigate to match summary/results screen
- Disable "Mark Payments" button after completion

---

#### 5. Cancel Match (Captain Only)

**Endpoint:** `POST /v2/mvp/matches/{matchId}/cancel`
**Auth Required:** Yes (Must be captain)

**Path Parameters:**
- `matchId` (UUID) - Match identifier

**Success Response:** `204 No Content`

**Side Effects:**
- Match status changes to `CANCELLED`
- Participants can no longer respond
- Emergency requests are invalidated

**Error Responses:**
```json
// 403 Forbidden - Not captain
{
  "code": "MVP-AUTH-010",
  "title": "Only the match captain can perform this action",
  "status": 403,
  "detail": "You must be the match creator to cancel this match"
}
```

**Frontend Notes:**
- Show only to captain
- Display confirmation dialog with warning
- Optionally allow adding cancellation reason (for future feature)
- Consider notifying all confirmed participants (future feature)
- After cancellation, show cancelled badge on match

---

### Invites

#### 1. Resolve Invite

**Endpoint:** `GET /v2/mvp/invites/{token}`
**Auth Required:** No (Public endpoint)

**Path Parameters:**
- `token` (string, 8 chars) - Invite token (e.g., `ABC12345`)

**Success Response:** `200 OK`
```json
{
  "matchId": "550e8400-e29b-41d4-a716-446655440000",
  "inviteType": "TEAM",
  "teamName": "Koramangala Knights",
  "groundMapsUrl": "https://maps.google.com/?q=@12.9352,77.6245",
  "startTime": "2026-02-01T15:00:00+05:30",
  "requiresAuth": false
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `matchId` | UUID | Match identifier (use for subsequent API calls) |
| `inviteType` | enum | `TEAM` or `EMERGENCY` |
| `teamName` | string | Team name |
| `groundMapsUrl` | string | Google Maps link |
| `startTime` | string | Match start time (ISO 8601) |
| `requiresAuth` | boolean | Always `false` (invite resolution is public) |

**Error Responses:**
```json
// 404 Not Found - Invalid token
{
  "code": "MVP-INVITE-001",
  "title": "Invite not found",
  "status": 404,
  "detail": "No invite found with this token. It may have expired or been deleted."
}

// 400 Bad Request - Expired invite
{
  "code": "MVP-INVITE-002",
  "title": "Invite has expired",
  "status": 400,
  "detail": "This invite link has expired"
}
```

**Frontend Flow:**

```
User clicks invite link
         │
         ▼
┌────────────────────┐
│ GET /invites/{token}│  ← No auth required
└─────────┬──────────┘
          │
          ▼
     ┌────────┐
     │ Success│
     └────┬───┘
          │
          ▼
┌──────────────────┐
│ Show match preview│
│ - Team name      │
│ - Location       │
│ - Date/time      │
│ - Invite type    │
└─────────┬────────┘
          │
          ▼
┌─────────────────┐
│ User authenticated?│
└─────────┬───────┘
      NO  │     YES
          │      │
          ▼      ▼
    ┌─────────┐ ┌──────────────┐
    │ Show OTP│ │ Show YES/NO  │
    │  screen │ │   buttons    │
    └─────────┘ └──────────────┘
```

**Frontend Notes:**
- Parse invite token from URL (e.g., `/invite/ABC12345`)
- Show match preview without requiring login
- Display countdown to match start time
- Show "TEAM INVITE" or "EMERGENCY INVITE" badge based on type
- If user not authenticated, show "Login to Respond" button
- After authentication, auto-navigate to response screen
- Handle expired invites gracefully with re-request option

---

### Emergency Requests

#### 1. Request Emergency Spot

**Endpoint:** `POST /v2/mvp/matches/{matchId}/emergency/request`
**Auth Required:** Yes

**Path Parameters:**
- `matchId` (UUID) - Match identifier

**No Request Body**

**Success Response:** `204 No Content`

**Side Effects:**
- Creates emergency request with 60-minute soft lock
- Lock expires automatically after 60 minutes
- User cannot make another emergency request while one is active

**Error Responses:**
```json
// 400 Bad Request - Emergency not enabled
{
  "code": "MVP-EMERGENCY-004",
  "title": "Emergency requests not enabled for this match",
  "status": 400,
  "detail": "The captain has not enabled emergency player requests"
}

// 400 Bad Request - Already requested
{
  "code": "MVP-EMERGENCY-002",
  "title": "User already has an active emergency request",
  "status": 400,
  "detail": "You already have a pending emergency request. Please wait for captain's response."
}

// 404 Not Found
{
  "code": "MVP-MATCH-001",
  "title": "Match not found",
  "status": 404,
  "detail": "No match found with the provided ID"
}
```

**Frontend Notes:**
- Show only if `emergencyEnabled === true`
- Display 60-minute countdown timer after request
- Disable button if user has active request
- Show "Request Sent - Waiting for Captain" state
- Poll or use WebSocket for real-time approval updates
- Show captain's decision (approved/rejected) immediately

---

#### 2. Get Pending Emergency Requests (Captain Only)

**Endpoint:** `GET /v2/mvp/matches/{matchId}/emergency/requests`
**Auth Required:** Yes (Must be captain)

**Path Parameters:**
- `matchId` (UUID) - Match identifier

**Success Response:** `200 OK`
```json
[
  {
    "requestId": 1,
    "userId": 5,
    "userName": "Rohit Patel",
    "phoneNumber": "+919876543215",
    "area": "Koramangala",
    "trustScore": 0,
    "status": "REQUESTED",
    "requestedAt": "2026-01-23T11:00:00+05:30",
    "lockExpiresAt": "2026-01-23T12:00:00+05:30"
  },
  {
    "requestId": 2,
    "userId": 6,
    "userName": "Sachin Rao",
    "phoneNumber": "+919876543216",
    "area": "Indiranagar",
    "trustScore": 5,
    "status": "REQUESTED",
    "requestedAt": "2026-01-23T11:15:00+05:30",
    "lockExpiresAt": "2026-01-23T12:15:00+05:30"
  }
]
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `requestId` | number | Emergency request identifier |
| `userId` | number | User who made the request |
| `userName` | string | User's display name |
| `phoneNumber` | string | User's phone number |
| `area` | string\|null | User's location |
| `trustScore` | number | Trust score (0-100, higher is better) |
| `status` | enum | `REQUESTED`, `APPROVED`, `REJECTED`, `EXPIRED` |
| `requestedAt` | string | When request was made (ISO 8601) |
| `lockExpiresAt` | string | When soft lock expires (ISO 8601) |

**Status Values:**
- `REQUESTED` - Pending captain's decision
- `APPROVED` - Captain approved
- `REJECTED` - Captain rejected
- `EXPIRED` - 60-minute lock expired

**Error Responses:**
```json
// 403 Forbidden - Not captain
{
  "code": "MVP-AUTH-010",
  "title": "Only the match captain can perform this action",
  "status": 403,
  "detail": "Only the captain can view emergency requests"
}
```

**Frontend Notes:**
- Show only to captain
- Display as sortable/filterable list
- Show countdown timer for each request's lock expiry
- Highlight requests about to expire (< 10 minutes)
- Show trust score as stars or progress bar
- Display area to prioritize nearby players
- Provide quick approve/reject actions
- Auto-refresh list every 30 seconds
- Show empty state if no requests

---

#### 3. Approve Emergency Request (Captain Only)

**Endpoint:** `POST /v2/mvp/matches/{matchId}/emergency/{requestId}/approve`
**Auth Required:** Yes (Must be captain)

**Path Parameters:**
- `matchId` (UUID) - Match identifier
- `requestId` (number) - Emergency request ID

**Success Response:** `204 No Content`

**Side Effects:**
- Request status changes to `APPROVED`
- User added to match participants with role `EMERGENCY`
- Emergency fee applied (if set, otherwise regular fee)

**Error Responses:**
```json
// 403 Forbidden - Not captain
{
  "code": "MVP-AUTH-010",
  "title": "Only the match captain can perform this action",
  "status": 403,
  "detail": "Only the captain can approve emergency requests"
}

// 400 Bad Request - Already processed
{
  "code": "MVP-EMERGENCY-005",
  "title": "Emergency request already processed",
  "status": 400,
  "detail": "This request has already been approved or rejected"
}

// 400 Bad Request - Lock expired
{
  "code": "MVP-EMERGENCY-003",
  "title": "Emergency request lock has expired",
  "status": 400,
  "detail": "The 60-minute lock has expired. User must request again."
}

// 404 Not Found
{
  "code": "MVP-EMERGENCY-001",
  "title": "Emergency request not found",
  "status": 404,
  "detail": "No emergency request found with this ID"
}
```

**Frontend Notes:**
- Show confirmation dialog before approval
- Display emergency fee amount in confirmation
- Remove from pending list after approval
- Show success toast/notification
- Update participant count immediately
- Consider sending notification to approved user (future)

---

#### 4. Reject Emergency Request (Captain Only)

**Endpoint:** `POST /v2/mvp/matches/{matchId}/emergency/{requestId}/reject`
**Auth Required:** Yes (Must be captain)

**Path Parameters:**
- `matchId` (UUID) - Match identifier
- `requestId` (number) - Emergency request ID

**Success Response:** `204 No Content`

**Side Effects:**
- Request status changes to `REJECTED`
- User can make another emergency request if needed

**Error Responses:**
```json
// 403 Forbidden - Not captain
{
  "code": "MVP-AUTH-010",
  "title": "Only the match captain can perform this action",
  "status": 403,
  "detail": "Only the captain can reject emergency requests"
}

// 400 Bad Request - Already processed
{
  "code": "MVP-EMERGENCY-005",
  "title": "Emergency request already processed",
  "status": 400,
  "detail": "This request has already been approved or rejected"
}

// 404 Not Found
{
  "code": "MVP-EMERGENCY-001",
  "title": "Emergency request not found",
  "status": 404,
  "detail": "No emergency request found with this ID"
}
```

**Frontend Notes:**
- Optionally allow captain to add rejection reason (future)
- Show confirmation dialog
- Remove from pending list after rejection
- Show success toast/notification
- Consider sending notification to rejected user (future)

---

### Backout Tracking

#### 1. Log Backout (Captain Only)

**Endpoint:** `POST /v2/mvp/matches/{matchId}/backout`
**Auth Required:** Yes (Must be captain)

**Path Parameters:**
- `matchId` (UUID) - Match identifier

**Request:**
```json
{
  "userId": 2,
  "reason": "GENUINE",
  "notes": "Family emergency"
}
```

**Field Specifications:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `userId` | number | Yes | User who backed out |
| `reason` | enum | Yes | Backout reason category |
| `notes` | string | No | Additional notes (max 500 chars) |

**Reason Enum Values:**
- `GENUINE` - Legitimate emergency/reason
- `CONFLICT` - Scheduling conflict
- `COMMUNICATION` - Miscommunication
- `PAYMENT` - Payment-related issue
- `NO_SHOW` - Didn't show up
- `CAPTAIN_DECISION` - Captain's decision to remove

**Success Response:** `204 No Content`

**Error Responses:**
```json
// 403 Forbidden - Not captain
{
  "code": "MVP-AUTH-010",
  "title": "Only the match captain can perform this action",
  "status": 403,
  "detail": "Only the captain can log backouts"
}

// 404 Not Found
{
  "code": "MVP-MATCH-001",
  "title": "Match not found",
  "status": 404,
  "detail": "No match found with the provided ID"
}
```

**Frontend Notes:**
- Show only to captain
- Display dropdown with reason categories
- Provide text area for optional notes
- Show in participant list context menu
- Don't auto-remove participant (captain's choice)
- Track for future trust score (not MVP)
- **Important:** Never auto-blacklist users based on backouts

---

### Payment Tracking

#### 1. Mark Payment (Captain Only)

**Endpoint:** `POST /v2/mvp/matches/{matchId}/payments/mark`
**Auth Required:** Yes (Must be captain)

**Path Parameters:**
- `matchId` (UUID) - Match identifier

**Request:**
```json
{
  "userId": 2,
  "paymentMode": "UPI"
}
```

**Field Specifications:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `userId` | number | Yes | User who paid |
| `paymentMode` | enum | No | Payment method used |

**Payment Mode Enum:**
- `CASH` - Cash payment
- `UPI` - UPI payment
- `null` - Not specified

**Success Response:** `204 No Content`

**Side Effects:**
- Participant's `paymentStatus` changes to `PAID`
- `paymentMode` is recorded

**Error Responses:**
```json
// 403 Forbidden - Not captain
{
  "code": "MVP-AUTH-010",
  "title": "Only the match captain can perform this action",
  "status": 403,
  "detail": "Only the captain can mark payments"
}

// 404 Not Found - Participant not found
{
  "code": "MVP-PARTICIPANT-001",
  "title": "Participant not found",
  "status": 404,
  "detail": "No participant found with this user ID"
}
```

**Frontend Notes:**
- Show only to captain
- Display in participant list with checkbox/toggle
- Show payment status with color coding:
  - Green: PAID
  - Red/Orange: UNPAID
- Show payment mode badge if available
- Quick-mark button in participant row
- Bulk payment marking option (future)
- Show total collected vs. expected amount
- **Important:** This is TRACKING ONLY - no payment gateway integration

---

## Data Models

### Core Types

#### Match Object (Full)
```typescript
interface Match {
  matchId: string;              // UUID
  teamName: string;
  eventType: EventType;
  ballCategory: BallCategory;
  ballVariant: BallVariant;
  groundMapsUrl: string;
  groundLat: number | null;
  groundLng: number | null;
  overs: number;
  feePerPerson: number;
  emergencyFee: number | null;
  requiredPlayers: number;
  backupSlots: number;
  emergencyEnabled: boolean;
  status: MatchStatus;
  startTime: string;            // ISO 8601
  createdAt: string;            // ISO 8601

  // Captain-only fields (null for others)
  captainId: number | null;
  captainName: string | null;
  captainPhone: string | null;

  // Always visible
  teamCount: number;
  backupCount: number;
  emergencyCount: number;

  // Captain-only (null for others)
  participants: Participant[] | null;
}

type EventType = 'PRACTICE' | 'TOURNAMENT' | 'NETS';
type BallCategory = 'LEATHER' | 'TENNIS';
type BallVariant = 'WHITE' | 'RED' | 'PINK' | 'HARD' | 'SOFT';
type MatchStatus = 'CREATED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
```

#### Participant Object
```typescript
interface Participant {
  userId: number;
  name: string;
  phoneNumber: string;
  role: ParticipantRole;
  status: ParticipantStatus;
  feeAmount: number;
  paymentStatus: PaymentStatus;
  paymentMode: PaymentMode | null;
}

type ParticipantRole = 'TEAM' | 'BACKUP' | 'EMERGENCY';
type ParticipantStatus = 'CONFIRMED' | 'BACKED_OUT' | 'NO_SHOW';
type PaymentStatus = 'PAID' | 'UNPAID';
type PaymentMode = 'CASH' | 'UPI';
```

#### Emergency Request Object
```typescript
interface EmergencyRequest {
  requestId: number;
  userId: number;
  userName: string;
  phoneNumber: string;
  area: string | null;
  trustScore: number;
  status: EmergencyRequestStatus;
  requestedAt: string;          // ISO 8601
  lockExpiresAt: string;        // ISO 8601
}

type EmergencyRequestStatus = 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'EXPIRED';
```

#### User Object
```typescript
interface MvpUser {
  userId: number;
  phoneNumber: string;
  name: string | null;
  area: string | null;
}
```

#### Auth Response
```typescript
interface OtpVerifyResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  phoneNumber: string;
  name: string | null;
  requiresProfile: boolean;
}
```

---

## Error Handling

### Error Response Format

All errors follow RFC 7807 Problem Details format:

```json
{
  "code": "MVP-MATCH-001",
  "title": "Match not found",
  "status": 404,
  "detail": "No match found with the provided ID"
}
```

### Error Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `code` | string | Machine-readable error code |
| `title` | string | Human-readable error summary |
| `status` | number | HTTP status code |
| `detail` | string | Detailed error message |

### Common HTTP Status Codes

| Code | Meaning | When It Occurs |
|------|---------|----------------|
| `200` | OK | Successful GET request with body |
| `204` | No Content | Successful POST/PUT/DELETE |
| `400` | Bad Request | Validation error, invalid data |
| `401` | Unauthorized | Missing or invalid token |
| `403` | Forbidden | Valid token but insufficient permissions |
| `404` | Not Found | Resource doesn't exist |
| `429` | Too Many Requests | Rate limit exceeded |
| `500` | Internal Server Error | Server error (contact support) |

### Error Code Categories

| Prefix | Category | Examples |
|--------|----------|----------|
| `MVP-AUTH-xxx` | Authentication | Invalid OTP, rate limit, unauthorized |
| `MVP-MATCH-xxx` | Match | Not found, full, invalid status |
| `MVP-INVITE-xxx` | Invites | Invalid token, expired |
| `MVP-EMERGENCY-xxx` | Emergency | Already requested, not enabled |
| `MVP-PARTICIPANT-xxx` | Participants | Not found |
| `MVP-USER-xxx` | Users | User not found |

### Error Handling Best Practices

#### 1. Display User-Friendly Messages

```typescript
// ❌ Bad - Show raw error
alert(error.detail);

// ✅ Good - User-friendly message
const friendlyMessages = {
  'MVP-AUTH-001': 'The OTP code you entered is incorrect. Please try again.',
  'MVP-AUTH-004': 'Too many OTP requests. Please wait 10 minutes.',
  'MVP-MATCH-002': 'This match is full. Try requesting as emergency player.',
  // ... more mappings
};

showToast(friendlyMessages[error.code] || 'Something went wrong. Please try again.');
```

#### 2. Handle Token Expiry

```typescript
// Intercept 401 errors
if (error.status === 401) {
  // Clear stored tokens
  clearAuthTokens();

  // Redirect to OTP screen
  redirectTo('/auth/otp');

  // Show message
  showToast('Your session expired. Please login again.');
}
```

#### 3. Retry Logic for Rate Limits

```typescript
if (error.code === 'MVP-AUTH-004') {
  // Extract wait time from error or use default
  const waitMinutes = 10;

  // Show countdown timer
  showCountdownTimer(waitMinutes, () => {
    // Enable retry after countdown
    enableRetryButton();
  });
}
```

#### 4. Network Error Handling

```typescript
try {
  const response = await fetch(url, options);
  // ... handle response
} catch (error) {
  if (error instanceof TypeError && error.message === 'Failed to fetch') {
    showToast('No internet connection. Please check your network.');
  } else {
    showToast('Something went wrong. Please try again.');
  }
}
```

---

## User Flows

### Flow 1: New User Joins Match via Invite

```
1. User receives WhatsApp message with invite link
   └─> https://app.gameteam.com/invite/ABC12345

2. User clicks link
   └─> App opens, calls GET /invites/ABC12345 (no auth)

3. App shows match preview
   ├─> Team: Koramangala Knights
   ├─> When: Feb 1, 2026 at 3:00 PM
   ├─> Where: [Map pin]
   └─> Fee: ₹200

4. User clicks "I'm In!"
   └─> App detects no auth token

5. App shows OTP screen
   └─> User enters phone: +919876543210

6. App calls POST /auth/otp/request
   └─> Success (204)

7. User receives OTP (hardcoded: 123456)
   └─> User enters: 123456

8. App calls POST /auth/otp/verify
   └─> Returns: { accessToken, requiresProfile: true }

9. App detects requiresProfile === true
   └─> Shows name input screen

10. User enters name: "Virat Kumar"
    └─> App calls POST /auth/profile

11. Profile updated, app redirects to match
    └─> Shows YES/NO buttons

12. User clicks "YES"
    └─> App calls POST /matches/{id}/respond with "YES"

13. Success!
    └─> App shows: "You're confirmed! See you on Feb 1"
    └─> Role assigned: TEAM (position 5/11)
```

### Flow 2: Captain Creates Match & Manages

```
1. Captain logged in, clicks "Create Match"

2. Fills match form:
   ├─> Team: Knights
   ├─> Type: Practice
   ├─> Ball: Leather White
   ├─> Ground: [Drops pin on map]
   ├─> Overs: 20
   ├─> Fee: ₹200
   ├─> Players: 11 + 2 backup
   └─> Emergency: Enabled

3. App calls POST /matches
   └─> Returns: {
       matchId: "uuid",
       teamInviteUrl: ".../ABC12345",
       emergencyInviteUrl: ".../XYZ67890"
   }

4. App shows share screen
   └─> Captain shares team invite via WhatsApp

5. Players start joining (see Flow 1)

6. Captain opens match dashboard
   └─> App calls GET /matches/{id}
   └─> Shows: 8/11 Team, 2/2 Backup, 0 Emergency

7. Emergency player requests spot
   └─> App shows notification badge to captain

8. Captain opens emergency requests
   └─> App calls GET /matches/{id}/emergency/requests
   └─> Shows: Rohit (Koramangala, Score: 5)

9. Captain approves
   └─> App calls POST /emergency/{id}/approve
   └─> Participant count updates: 8/11 Team, 2/2 Backup, 1 Emergency

10. Match day arrives, players show up
    └─> Captain marks payments as received
    └─> App calls POST /payments/mark for each player

11. Match completes
    └─> Captain clicks "Complete Match"
    └─> App calls POST /matches/{id}/complete
    └─> Platform fee ₹50 recorded
```

### Flow 3: Emergency Player Request

```
1. User receives emergency invite link
   └─> https://app.gameteam.com/invite/XYZ67890

2. User clicks, app calls GET /invites/XYZ67890
   └─> Shows: "EMERGENCY INVITE" badge

3. User authenticates (if needed)

4. App shows emergency request screen
   ├─> Match details
   ├─> Emergency fee: ₹300
   └─> "Request to Play" button

5. User clicks "Request to Play"
   └─> App calls POST /matches/{id}/emergency/request

6. Success! App shows waiting screen
   ├─> "Request sent to captain"
   ├─> Countdown: 59:30 remaining
   └─> Captain decision: Pending...

7. Captain approves (see Flow 2)

8. App receives update (polling or WebSocket)
   └─> Shows: "Approved! You're in as emergency player"
   └─> Fee: ₹300
```

### Flow 4: Player Changes Mind (YES → NO)

```
1. User previously confirmed (YES)

2. User opens match, clicks "Can't Make It"
   └─> App shows confirmation:
       "You previously confirmed. Are you sure you can't play?"

3. User confirms
   └─> App calls POST /matches/{id}/respond with "NO"

4. Success
   ├─> Participant removed from match
   ├─> Slot becomes available
   └─> App shows: "You've been marked as unavailable"

5. Later, captain may log backout reason
   └─> Captain calls POST /matches/{id}/backout
   └─> Reason: "GENUINE", Notes: "Work emergency"
```

---

## State Management

### Authentication State

```typescript
interface AuthState {
  isAuthenticated: boolean;
  accessToken: string | null;
  refreshToken: string | null;
  user: {
    userId: number;
    phoneNumber: string;
    name: string | null;
    area: string | null;
  } | null;
}

// Initial state
const initialAuthState: AuthState = {
  isAuthenticated: false,
  accessToken: null,
  refreshToken: null,
  user: null
};
```

### Match State

```typescript
interface MatchState {
  currentMatch: Match | null;
  isLoading: boolean;
  error: string | null;
  userRole: 'captain' | 'participant' | 'visitor' | null;
  userResponse: 'YES' | 'NO' | null;
}

// Derived values
const isCapt ain = currentMatch?.captainId === authState.user?.userId;
const canManage = userRole === 'captain';
const hasResponded = userResponse !== null;
```

### Emergency Request State

```typescript
interface EmergencyState {
  hasActiveRequest: boolean;
  currentRequest: {
    requestId: number;
    status: EmergencyRequestStatus;
    lockExpiresAt: string;
  } | null;
  pendingRequests: EmergencyRequest[];  // Captain only
}
```

### UI State Guidelines

#### 1. Loading States

```typescript
// Show loading spinner
isLoading: boolean

// Loading states for different actions
const loadingStates = {
  fetchingMatch: boolean,
  responding: boolean,
  approvingEmergency: boolean,
  markingPayment: boolean,
  completingMatch: boolean
};
```

#### 2. Error States

```typescript
// Error per action
const errors = {
  matchError: string | null,
  responseError: string | null,
  emergencyError: string | null
};

// Clear errors on retry
const clearError = (errorType: keyof typeof errors) => {
  errors[errorType] = null;
};
```

#### 3. Optimistic Updates

```typescript
// Example: Mark payment optimistically
const markPayment = async (userId: number, mode: PaymentMode) => {
  // 1. Optimistically update UI
  updateParticipantPaymentStatus(userId, 'PAID', mode);

  try {
    // 2. Call API
    await api.markPayment(matchId, userId, mode);

    // 3. Success - UI already updated!
    showToast('Payment marked');

  } catch (error) {
    // 4. Revert on error
    updateParticipantPaymentStatus(userId, 'UNPAID', null);
    showError(error);
  }
};
```

---

## Edge Cases & Validations

### 1. Match Capacity Management

**Scenario:** Match fills up while user is viewing

```typescript
// Before responding YES
const checkCapacity = async () => {
  // Fetch latest match data
  const match = await api.getMatch(matchId);

  const totalSlots = match.requiredPlayers + match.backupSlots;
  const filledSlots = match.teamCount + match.backupCount;

  if (filledSlots >= totalSlots) {
    showAlert('Match is now full. Try emergency request?');
    return false;
  }

  return true;
};
```

**UI Recommendation:**
- Show real-time slot availability
- Update every 30 seconds or on visibility change
- Disable YES button if full
- Suggest emergency request as alternative

### 2. Timezone Handling

**All timestamps are in ISO 8601 with offset:**

```typescript
// From API: "2026-02-01T15:00:00+05:30"

// Display in user's local timezone
const displayTime = (isoString: string) => {
  const date = new Date(isoString);
  return date.toLocaleString('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short'
  });
  // Output: "1 Feb 2026, 3:00 PM"
};

// Show relative time for recent events
const relativeTime = (isoString: string) => {
  const now = new Date();
  const then = new Date(isoString);
  const diffMs = now.getTime() - then.getTime();
  const diffMins = Math.floor(diffMs / 60000);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins} min ago`;
  // ... etc
};
```

### 3. OTP Rate Limiting

**Track and display remaining attempts:**

```typescript
interface OtpState {
  requestCount: number;
  windowStart: Date | null;
  canRequest: boolean;
  nextRequestAt: Date | null;
}

const checkOtpRateLimit = (state: OtpState) => {
  if (!state.windowStart) return true;

  const windowDuration = 10 * 60 * 1000; // 10 minutes
  const now = Date.now();
  const windowEnd = state.windowStart.getTime() + windowDuration;

  if (now > windowEnd) {
    // Window expired, reset
    return true;
  }

  if (state.requestCount >= 3) {
    // Rate limited
    state.nextRequestAt = new Date(windowEnd);
    return false;
  }

  return true;
};
```

**UI Recommendation:**
- Show "3 requests remaining" before rate limit
- Display countdown: "Try again in 8:45"
- Suggest using different phone number

### 4. Emergency Lock Expiry

**Auto-update UI when lock expires:**

```typescript
const startExpiryCountdown = (lockExpiresAt: string) => {
  const expiryTime = new Date(lockExpiresAt).getTime();

  const interval = setInterval(() => {
    const now = Date.now();
    const remaining = expiryTime - now;

    if (remaining <= 0) {
      clearInterval(interval);
      // Update UI to show expired
      updateRequestStatus('EXPIRED');
      return;
    }

    // Update countdown display
    const minutes = Math.floor(remaining / 60000);
    const seconds = Math.floor((remaining % 60000) / 1000);
    updateCountdown(`${minutes}:${seconds.toString().padStart(2, '0')}`);
  }, 1000);
};
```

**UI States:**
- Active: "⏱️ 45:30 remaining"
- Expiring soon: "⚠️ 5:00 remaining" (yellow)
- Expired: "❌ Request expired" (red)

### 5. Concurrent Captain Actions

**Scenario:** Two captains (shouldn't happen, but defensive coding)

```typescript
// Server enforces captain check
// Frontend should disable actions if not captain

const canPerformAction = (action: string, match: Match, userId: number) => {
  const isCaptain = match.captainId === userId;

  const captainOnlyActions = [
    'complete-match',
    'cancel-match',
    'approve-emergency',
    'reject-emergency',
    'mark-payment',
    'log-backout'
  ];

  if (captainOnlyActions.includes(action) && !isCaptain) {
    return false;
  }

  return true;
};
```

### 6. Stale Data Handling

**Implement cache invalidation:**

```typescript
// Invalidate and refetch after mutations
const respondToMatch = async (response: 'YES' | 'NO') => {
  try {
    await api.respondToMatch(matchId, response);

    // Invalidate cache and refetch
    await invalidateMatchCache(matchId);
    const updatedMatch = await api.getMatch(matchId);

    updateMatchState(updatedMatch);
  } catch (error) {
    handleError(error);
  }
};

// Periodic refresh for captain dashboard
useEffect(() => {
  if (isCaptain) {
    const interval = setInterval(() => {
      fetchMatch(matchId);
      fetchEmergencyRequests(matchId);
    }, 30000); // Every 30 seconds

    return () => clearInterval(interval);
  }
}, [isCaptain, matchId]);
```

### 7. Network Offline Handling

```typescript
// Detect offline status
const isOnline = navigator.onLine;

// Queue actions when offline
const offlineQueue: Action[] = [];

const queueAction = (action: Action) => {
  if (!isOnline) {
    offlineQueue.push(action);
    showToast('Action queued. Will sync when online.');
    return;
  }

  performAction(action);
};

// Sync when back online
window.addEventListener('online', async () => {
  showToast('Back online. Syncing...');

  for (const action of offlineQueue) {
    try {
      await performAction(action);
    } catch (error) {
      // Handle failed action
    }
  }

  offlineQueue.length = 0;
});
```

### 8. Form Validation Examples

**Create Match Form:**

```typescript
const validateMatchForm = (data: CreateMatchDto) => {
  const errors: Record<string, string> = {};

  // Team name
  if (!data.teamName || data.teamName.length < 2) {
    errors.teamName = 'Team name must be at least 2 characters';
  }

  // Start time
  const startTime = new Date(data.startTime);
  if (startTime <= new Date()) {
    errors.startTime = 'Start time must be in the future';
  }

  // Overs
  if (data.overs < 1 || data.overs > 50) {
    errors.overs = 'Overs must be between 1 and 50';
  }

  // Fee
  if (data.feePerPerson < 0) {
    errors.feePerPerson = 'Fee cannot be negative';
  }

  // Ball variant validation
  if (data.ballCategory === 'LEATHER') {
    if (!['WHITE', 'RED', 'PINK'].includes(data.ballVariant)) {
      errors.ballVariant = 'Invalid variant for leather ball';
    }
  }

  if (data.ballCategory === 'TENNIS') {
    if (!['HARD', 'SOFT'].includes(data.ballVariant)) {
      errors.ballVariant = 'Invalid variant for tennis ball';
    }
  }

  return errors;
};
```

---

## Testing Checklist for Frontend

### Authentication Flow
- [ ] Request OTP with valid phone number
- [ ] Request OTP with invalid phone number (should fail)
- [ ] Verify OTP with correct code
- [ ] Verify OTP with wrong code (show attempts remaining)
- [ ] Verify OTP after 5 failed attempts (should block)
- [ ] Request OTP 4 times in 10 minutes (4th should fail)
- [ ] Update profile after OTP verification
- [ ] Skip profile update if name already exists

### Match Creation
- [ ] Create match with all required fields
- [ ] Create match with future start time
- [ ] Create match with past start time (should fail)
- [ ] Create match with emergency enabled
- [ ] Create match with emergency disabled
- [ ] Validate ball variant matches ball category
- [ ] Share invite links (team and emergency)

### Match Response
- [ ] Respond YES to match (check role assignment)
- [ ] Respond YES when match is full (should fail)
- [ ] Respond NO to match
- [ ] Change response from YES to NO
- [ ] Change response from NO to YES
- [ ] Multiple YES responses (should be idempotent)

### Emergency Flow
- [ ] Request emergency spot
- [ ] Request when emergency disabled (should fail)
- [ ] Request when already have active request (should fail)
- [ ] Captain views pending requests
- [ ] Captain approves request (user becomes participant)
- [ ] Captain rejects request
- [ ] Request expires after 60 minutes

### Captain Actions
- [ ] Complete match (only captain can)
- [ ] Cancel match (only captain can)
- [ ] Mark payment for participant
- [ ] Log backout with reason
- [ ] Approve emergency request
- [ ] Reject emergency request
- [ ] View full participant list (non-captain sees counts only)

### Edge Cases
- [ ] Handle token expiry gracefully
- [ ] Handle network offline/online
- [ ] Handle stale data (refresh after mutation)
- [ ] Handle concurrent updates
- [ ] Handle rate limiting
- [ ] Display user-friendly error messages
- [ ] Optimistic UI updates

---

## Quick Reference

### Base URLs
- **Development:** `http://localhost:8080`
- **Production:** `https://api.gameteam.com` (when deployed)

### Authentication Header
```
Authorization: Bearer <accessToken>
```

### Content Type
```
Content-Type: application/json
```

### Hardcoded Values (MVP)
- **OTP Code:** `123456`
- **OTP Expiry:** 5 minutes
- **Max OTP Attempts:** 5
- **Rate Limit:** 3 requests per 10 minutes
- **Emergency Lock:** 60 minutes
- **Platform Fee:** ₹50

### Key Response Codes
- `200` - Success with data
- `204` - Success, no content
- `400` - Bad request / Validation error
- `401` - Unauthorized
- `403` - Forbidden (not captain)
- `404` - Not found
- `429` - Rate limit exceeded

---

## Support & Questions

**Backend Developer Contact:** [Your Name]
**API Issues:** Report to backend team
**Documentation Version:** 1.0
**Last Updated:** January 23, 2026

**Related Documents:**
- `POSTMAN_TESTING_GUIDE.md` - For API testing with Postman
- `QUICK_REFERENCE.md` - Quick lookup guide
- Implementation Plan - `/Users/sudranjan/.claude/plans/logical-bubbling-honey.md`

---

## Changelog

### Version 1.0 (Jan 23, 2026)
- Initial API specification
- All 14 MVP endpoints documented
- Complete data models
- Error handling guide
- User flow diagrams
- Edge case handling
- Testing checklist

---

**End of Document**

Happy coding! 🚀
