# Frontend Player Flow Implementation Guide

**Version:** 1.0  
**Date:** January 23, 2026  
**Status:** Ready for Implementation  
**Author:** Lead Frontend Engineer

---

## Table of Contents

1. [Overview](#overview)
2. [URL Generation Flow](#1-url-generation-flow)
3. [Player Journey When Opening Link](#2-player-journey-when-opening-link)
4. [Complete API Reference](#3-complete-api-reference)
5. [Frontend Implementation Details](#4-frontend-implementation-details)
6. [State Management](#5-state-management)
7. [Error Handling](#6-error-handling)
8. [Testing Checklist](#7-testing-checklist)

---

## Overview

This document provides complete guidance for implementing the player flow when a match is created and shared. It covers:

1. **URL Generation** - How invite URLs are created after match creation
2. **Player Journey** - What happens when a random player opens the shared link
3. **API Integration** - Which APIs to call and when
4. **Response Handling** - How players respond to match invitations

### Current System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         MATCH CREATION                               │
│  Captain creates match → Backend generates invite tokens/URLs        │
│  Two types of invites: TEAM invite & EMERGENCY invite               │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         URL SHARING                                  │
│  Captain shares invite URL via WhatsApp/SMS/etc.                    │
│  URL format: {baseUrl}/v2/mvp/invites/{8-char-token}                │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         PLAYER OPENS LINK                            │
│  1. Resolve invite token → Get match details                        │
│  2. Show match preview (no auth required)                           │
│  3. User authenticates (if needed) → Responds YES/NO                │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 1. URL Generation Flow

### When Match is Created

When a captain creates a match using `POST /v2/mvp/matches`, the backend automatically:

1. Creates the match record
2. Generates a **TEAM invite** (always)
3. Generates an **EMERGENCY invite** (if `emergencyEnabled: true`)
4. Returns both URLs in the response

### API: Create Match

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

**Response:** `200 OK`
```json
{
  "matchId": "550e8400-e29b-41d4-a716-446655440000",
  "teamInviteUrl": "http://localhost:8080/v2/mvp/invites/ABC12345",
  "emergencyInviteUrl": "http://localhost:8080/v2/mvp/invites/XYZ67890"
}
```

### URL Structure

| Component | Description | Example |
|-----------|-------------|---------|
| Base URL | Server base URL | `http://localhost:8080` (dev) |
| Path | Invite endpoint | `/v2/mvp/invites/` |
| Token | 8-character unique token | `ABC12345` |

**Full URL Example:**
- Team Invite: `http://localhost:8080/v2/mvp/invites/ABC12345`
- Emergency Invite: `http://localhost:8080/v2/mvp/invites/XYZ67890`

### Frontend URL Handling

Since you're building a frontend app, you'll want to create **shareable deep links** that route to your app:

```typescript
// Option 1: Use backend URLs directly (for API testing)
const teamInviteUrl = response.teamInviteUrl;
// "http://localhost:8080/v2/mvp/invites/ABC12345"

// Option 2: Create frontend app URLs (recommended for production)
const createShareableUrl = (backendUrl: string) => {
  const token = backendUrl.split('/').pop(); // Extract token
  return `https://yourapp.com/invite/${token}`;
};

// Result: "https://yourapp.com/invite/ABC12345"
```

### Frontend Implementation After Match Creation

```typescript
// After successful match creation
const handleMatchCreated = (response: MatchCreatedResponse) => {
  // 1. Store match ID
  setCurrentMatchId(response.matchId);
  
  // 2. Extract tokens for frontend URLs
  const teamToken = extractToken(response.teamInviteUrl);
  const emergencyToken = response.emergencyInviteUrl 
    ? extractToken(response.emergencyInviteUrl) 
    : null;
  
  // 3. Create shareable URLs (your frontend app URLs)
  const shareUrls = {
    team: `${FRONTEND_BASE_URL}/invite/${teamToken}`,
    emergency: emergencyToken 
      ? `${FRONTEND_BASE_URL}/invite/${emergencyToken}` 
      : null
  };
  
  // 4. Show share options
  showShareScreen(shareUrls);
};

// Helper function
const extractToken = (url: string): string => {
  return url.split('/').pop() || '';
};
```

### Share Screen UI

```typescript
// Share screen component
const ShareScreen = ({ shareUrls }) => {
  const copyToClipboard = (url: string) => {
    navigator.clipboard.writeText(url);
    showToast('Link copied!');
  };

  const shareViaWhatsApp = (url: string, type: 'team' | 'emergency') => {
    const message = type === 'team'
      ? `Join our cricket match! 🏏\n${url}`
      : `Emergency player needed! 🚨\n${url}`;
    
    const whatsappUrl = `https://wa.me/?text=${encodeURIComponent(message)}`;
    window.open(whatsappUrl, '_blank');
  };

  return (
    <div>
      <h2>Match Created! 🎉</h2>
      
      {/* Team Invite */}
      <div className="invite-section">
        <h3>Team Invite Link</h3>
        <p>Share with your team members:</p>
        <input readOnly value={shareUrls.team} />
        <button onClick={() => copyToClipboard(shareUrls.team)}>
          📋 Copy
        </button>
        <button onClick={() => shareViaWhatsApp(shareUrls.team, 'team')}>
          📱 WhatsApp
        </button>
      </div>
      
      {/* Emergency Invite (if enabled) */}
      {shareUrls.emergency && (
        <div className="invite-section">
          <h3>Emergency Invite Link</h3>
          <p>For emergency player pool:</p>
          <input readOnly value={shareUrls.emergency} />
          <button onClick={() => copyToClipboard(shareUrls.emergency)}>
            📋 Copy
          </button>
          <button onClick={() => shareViaWhatsApp(shareUrls.emergency, 'emergency')}>
            📱 WhatsApp
          </button>
        </div>
      )}
    </div>
  );
};
```

---

## 2. Player Journey When Opening Link

### Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│  STEP 1: Player clicks invite link                                      │
│  URL: https://yourapp.com/invite/ABC12345                               │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  STEP 2: Frontend extracts token from URL                               │
│  Token: "ABC12345"                                                      │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  STEP 3: Call API to resolve invite (NO AUTH REQUIRED)                  │
│  GET /v2/mvp/invites/ABC12345                                           │
│                                                                         │
│  Response:                                                              │
│  {                                                                      │
│    "matchId": "550e8400-e29b-41d4-a716-446655440000",                   │
│    "inviteType": "TEAM",                                                │
│    "teamName": "Koramangala Knights",                                   │
│    "groundMapsUrl": "https://maps.google.com/...",                      │
│    "startTime": "2026-02-01T15:00:00+05:30",                            │
│    "requiresAuth": false                                                │
│  }                                                                      │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  STEP 4: Show Match Preview Screen (NO AUTH REQUIRED)                   │
│                                                                         │
│  ┌───────────────────────────────────────────────┐                      │
│  │  🏏 TEAM INVITE                               │                      │
│  │                                               │                      │
│  │  Koramangala Knights                          │                      │
│  │                                               │                      │
│  │  📅 Feb 1, 2026 at 3:00 PM                    │                      │
│  │  📍 [Map Preview]                             │                      │
│  │                                               │                      │
│  │  [   I'm In!   ]  [  Can't Make It  ]         │                      │
│  └───────────────────────────────────────────────┘                      │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  STEP 5: User clicks "I'm In!" or "Can't Make It"                       │
│  → Check if authenticated                                               │
└─────────────────────────────────────────────────────────────────────────┘
                          │                    │
            NOT AUTHENTICATED            AUTHENTICATED
                          │                    │
                          ▼                    ▼
┌─────────────────────────────────┐  ┌─────────────────────────────────┐
│  STEP 5a: Show OTP Login        │  │  STEP 5b: Call respond API      │
│                                 │  │  POST /v2/mvp/matches/{id}/     │
│  1. Request OTP                 │  │        respond                  │
│  2. Verify OTP                  │  │  Body: { "response": "YES" }    │
│  3. Set profile (if new user)   │  │                                 │
│  4. Then call respond API       │  │                                 │
└─────────────────────────────────┘  └─────────────────────────────────┘
                          │                    │
                          └────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  STEP 6: Show Confirmation Screen                                       │
│                                                                         │
│  ┌───────────────────────────────────────────────┐                      │
│  │  ✅ You're In!                                │                      │
│  │                                               │                      │
│  │  Role: TEAM (Position 5/11)                   │                      │
│  │  Fee: ₹200                                    │                      │
│  │                                               │                      │
│  │  See you on Feb 1, 2026!                      │                      │
│  │                                               │                      │
│  │  [  View Match Details  ]                     │                      │
│  └───────────────────────────────────────────────┘                      │
└─────────────────────────────────────────────────────────────────────────┘
```

### Step-by-Step Implementation

#### Step 1: Route Configuration

```typescript
// React Router example
const routes = [
  {
    path: '/invite/:token',
    component: InviteScreen,
  },
  // ... other routes
];

// Vue Router example
const routes = [
  {
    path: '/invite/:token',
    component: InviteScreen,
  },
];
```

#### Step 2-3: Invite Screen Component

```typescript
// InviteScreen.tsx
import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';

interface InviteData {
  matchId: string;
  inviteType: 'TEAM' | 'EMERGENCY';
  teamName: string;
  groundMapsUrl: string;
  startTime: string;
  requiresAuth: boolean;
}

const InviteScreen = () => {
  const { token } = useParams<{ token: string }>();
  const [inviteData, setInviteData] = useState<InviteData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    resolveInvite();
  }, [token]);

  const resolveInvite = async () => {
    try {
      setLoading(true);
      
      // API call - NO AUTH REQUIRED
      const response = await fetch(
        `${API_BASE_URL}/v2/mvp/invites/${token}`
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.detail || 'Invalid invite link');
      }

      const data: InviteData = await response.json();
      setInviteData(data);
      
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return <ErrorScreen message={error} />;
  }

  return (
    <MatchPreview 
      inviteData={inviteData!}
      token={token!}
    />
  );
};
```

#### Step 4: Match Preview Component

```typescript
// MatchPreview.tsx
interface MatchPreviewProps {
  inviteData: InviteData;
  token: string;
}

const MatchPreview = ({ inviteData, token }: MatchPreviewProps) => {
  const { isAuthenticated, user } = useAuth();
  const [responding, setResponding] = useState(false);

  // Parse coordinates from Google Maps URL for map preview
  const parseCoordinates = (url: string) => {
    const match = url.match(/@(-?\d+\.?\d*),(-?\d+\.?\d*)/);
    if (match) {
      return { lat: parseFloat(match[1]), lng: parseFloat(match[2]) };
    }
    return null;
  };

  const coordinates = parseCoordinates(inviteData.groundMapsUrl);

  // Format date for display
  const formatDateTime = (isoString: string) => {
    const date = new Date(isoString);
    return date.toLocaleString('en-IN', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleResponse = async (response: 'YES' | 'NO') => {
    if (!isAuthenticated) {
      // Store intended action and redirect to auth
      sessionStorage.setItem('pendingResponse', JSON.stringify({
        matchId: inviteData.matchId,
        response,
        token,
      }));
      navigate('/auth/login');
      return;
    }

    await submitResponse(response);
  };

  const submitResponse = async (response: 'YES' | 'NO') => {
    try {
      setResponding(true);
      
      const res = await fetch(
        `${API_BASE_URL}/v2/mvp/matches/${inviteData.matchId}/respond`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getAccessToken()}`,
          },
          body: JSON.stringify({ response }),
        }
      );

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.detail);
      }

      // Navigate to confirmation screen
      navigate(`/match/${inviteData.matchId}/confirmed`, {
        state: { response }
      });

    } catch (err) {
      showError(err.message);
    } finally {
      setResponding(false);
    }
  };

  return (
    <div className="match-preview">
      {/* Invite Type Badge */}
      <div className={`badge ${inviteData.inviteType.toLowerCase()}`}>
        {inviteData.inviteType === 'TEAM' ? '🏏 TEAM INVITE' : '🚨 EMERGENCY INVITE'}
      </div>

      {/* Team Name */}
      <h1>{inviteData.teamName}</h1>

      {/* Date & Time */}
      <div className="info-row">
        <span className="icon">📅</span>
        <span>{formatDateTime(inviteData.startTime)}</span>
      </div>

      {/* Location Map Preview */}
      {coordinates && (
        <div className="map-preview">
          <MapComponent 
            lat={coordinates.lat} 
            lng={coordinates.lng}
            height={200}
          />
          <a 
            href={inviteData.groundMapsUrl} 
            target="_blank" 
            rel="noopener noreferrer"
          >
            Open in Google Maps →
          </a>
        </div>
      )}

      {/* Response Buttons */}
      <div className="response-buttons">
        <button 
          className="btn-primary"
          onClick={() => handleResponse('YES')}
          disabled={responding}
        >
          {responding ? 'Submitting...' : "I'm In! ✅"}
        </button>
        
        <button 
          className="btn-secondary"
          onClick={() => handleResponse('NO')}
          disabled={responding}
        >
          Can't Make It ❌
        </button>
      </div>

      {/* Login hint for unauthenticated users */}
      {!isAuthenticated && (
        <p className="hint">
          You'll need to verify your phone number to respond
        </p>
      )}
    </div>
  );
};
```

#### Step 5a: Authentication Flow (If Not Logged In)

```typescript
// AuthFlow.tsx - Handle pending response after login
const AuthFlow = () => {
  const { isAuthenticated } = useAuth();
  
  useEffect(() => {
    if (isAuthenticated) {
      // Check for pending response
      const pendingResponse = sessionStorage.getItem('pendingResponse');
      if (pendingResponse) {
        const { matchId, response } = JSON.parse(pendingResponse);
        sessionStorage.removeItem('pendingResponse');
        
        // Submit the pending response
        submitPendingResponse(matchId, response);
      }
    }
  }, [isAuthenticated]);

  const submitPendingResponse = async (matchId: string, response: string) => {
    try {
      await fetch(
        `${API_BASE_URL}/v2/mvp/matches/${matchId}/respond`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getAccessToken()}`,
          },
          body: JSON.stringify({ response }),
        }
      );
      
      navigate(`/match/${matchId}/confirmed`, { state: { response } });
    } catch (err) {
      showError(err.message);
    }
  };
};
```

#### Step 6: Confirmation Screen

```typescript
// ConfirmationScreen.tsx
const ConfirmationScreen = () => {
  const { matchId } = useParams();
  const location = useLocation();
  const response = location.state?.response;
  const [matchDetails, setMatchDetails] = useState(null);

  useEffect(() => {
    fetchMatchDetails();
  }, [matchId]);

  const fetchMatchDetails = async () => {
    const res = await fetch(
      `${API_BASE_URL}/v2/mvp/matches/${matchId}`,
      {
        headers: {
          'Authorization': `Bearer ${getAccessToken()}`,
        },
      }
    );
    const data = await res.json();
    setMatchDetails(data);
  };

  if (response === 'NO') {
    return (
      <div className="confirmation">
        <h1>Got it! 👍</h1>
        <p>You've been marked as unavailable for this match.</p>
        <button onClick={() => navigate('/my-games')}>
          View My Games
        </button>
      </div>
    );
  }

  return (
    <div className="confirmation">
      <h1>You're In! ✅</h1>
      
      {matchDetails && (
        <>
          <div className="detail">
            <strong>Team:</strong> {matchDetails.teamName}
          </div>
          
          <div className="detail">
            <strong>Date:</strong> {formatDateTime(matchDetails.startTime)}
          </div>
          
          <div className="detail">
            <strong>Fee:</strong> ₹{matchDetails.feePerPerson}
          </div>
          
          <div className="slot-info">
            <strong>Current Status:</strong>
            <span>{matchDetails.teamCount}/{matchDetails.requiredPlayers} Team</span>
            <span>{matchDetails.backupCount}/{matchDetails.backupSlots} Backup</span>
          </div>
        </>
      )}

      <button onClick={() => navigate(`/match/${matchId}`)}>
        View Match Details
      </button>
      
      <button onClick={() => navigate('/my-games')}>
        My Games
      </button>
    </div>
  );
};
```

---

## 3. Complete API Reference

### APIs Used in Player Flow

| # | Endpoint | Auth | Purpose |
|---|----------|------|---------|
| 1 | `GET /v2/mvp/invites/{token}` | ❌ No | Resolve invite link |
| 2 | `POST /v2/mvp/auth/otp/request` | ❌ No | Request OTP |
| 3 | `POST /v2/mvp/auth/otp/verify` | ❌ No | Verify OTP |
| 4 | `POST /v2/mvp/auth/profile` | ✅ Yes | Set user profile |
| 5 | `GET /v2/mvp/matches/{matchId}` | 🔶 Optional | Get match details |
| 6 | `POST /v2/mvp/matches/{matchId}/respond` | ✅ Yes | Respond YES/NO |
| 7 | `GET /v2/mvp/matches/my-games` | ✅ Yes | Get user's games |

### API 1: Resolve Invite

**Endpoint:** `GET /v2/mvp/invites/{token}`  
**Auth:** Not required  
**Purpose:** Convert invite token to match details

**Response:**
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

**Errors:**
| Code | Status | Description |
|------|--------|-------------|
| `MVP-INVITE-001` | 404 | Invite not found |
| `MVP-INVITE-002` | 400 | Invite expired |

### API 2: Request OTP

**Endpoint:** `POST /v2/mvp/auth/otp/request`  
**Auth:** Not required

**Request:**
```json
{
  "phoneNumber": "+919876543210"
}
```

**Response:** `204 No Content`

**Note:** For MVP, OTP is hardcoded as `123456`

### API 3: Verify OTP

**Endpoint:** `POST /v2/mvp/auth/otp/verify`  
**Auth:** Not required

**Request:**
```json
{
  "phoneNumber": "+919876543210",
  "otpCode": "123456"
}
```

**Response:**
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

**Important:** If `requiresProfile: true`, redirect to profile setup.

### API 4: Set Profile

**Endpoint:** `POST /v2/mvp/auth/profile`  
**Auth:** Required

**Request:**
```json
{
  "name": "Rahul Sharma",
  "area": "Koramangala"
}
```

**Response:** `204 No Content`

### API 5: Get Match Details

**Endpoint:** `GET /v2/mvp/matches/{matchId}`  
**Auth:** Optional (affects response detail)

**Response (Public View):**
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

**Note:** Captain-only fields (`captainId`, `captainName`, `captainPhone`, `participants`) are `null` for non-captain views.

### API 6: Respond to Match

**Endpoint:** `POST /v2/mvp/matches/{matchId}/respond`  
**Auth:** Required

**Request:**
```json
{
  "response": "YES"
}
```

**Valid Values:** `"YES"` or `"NO"`

**Response:** `204 No Content`

**Behavior:**
- `YES`: User is added as participant (TEAM → BACKUP based on availability)
- `NO`: User is marked unavailable, removed from participants if previously confirmed

**Errors:**
| Code | Status | Description |
|------|--------|-------------|
| `MVP-MATCH-001` | 404 | Match not found |
| `MVP-MATCH-002` | 400 | Match is full |
| `MVP-MATCH-005` | 400 | Invalid match status |

### API 7: Get My Games

**Endpoint:** `GET /v2/mvp/matches/my-games`  
**Auth:** Required

**Response:**
```json
{
  "games": [
    {
      "matchId": "550e8400-e29b-41d4-a716-446655440000",
      "teamName": "Koramangala Knights",
      "eventType": "PRACTICE",
      "status": "CREATED",
      "startTime": "2026-02-01T15:00:00+05:30",
      "userRole": "TEAM",
      "isCaptain": false,
      "teamCount": 8,
      "backupCount": 2,
      "requiredPlayers": 11,
      "paymentStatus": "UNPAID",
      "feeAmount": 200
    }
  ],
  "totalCount": 1,
  "upcomingCount": 1,
  "completedCount": 0,
  "cancelledCount": 0
}
```

---

## 4. Frontend Implementation Details

### TypeScript Interfaces

```typescript
// types/invite.ts
export interface InviteResponse {
  matchId: string;
  inviteType: 'TEAM' | 'EMERGENCY';
  teamName: string;
  groundMapsUrl: string;
  startTime: string;
  requiresAuth: boolean;
}

// types/match.ts
export type EventType = 'PRACTICE' | 'TOURNAMENT' | 'NETS';
export type BallCategory = 'LEATHER' | 'TENNIS';
export type BallVariant = 'WHITE' | 'RED' | 'PINK' | 'HARD' | 'SOFT';
export type MatchStatus = 'CREATED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
export type ParticipantRole = 'TEAM' | 'BACKUP' | 'EMERGENCY';
export type PaymentStatus = 'PAID' | 'UNPAID';

export interface MatchResponse {
  matchId: string;
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
  startTime: string;
  createdAt: string;
  captainId: number | null;
  captainName: string | null;
  captainPhone: string | null;
  teamCount: number;
  backupCount: number;
  emergencyCount: number;
  participants: Participant[] | null;
}

export interface Participant {
  userId: number;
  name: string;
  phoneNumber: string;
  role: ParticipantRole;
  status: 'CONFIRMED' | 'BACKED_OUT' | 'NO_SHOW';
  feeAmount: number;
  paymentStatus: PaymentStatus;
  paymentMode: 'CASH' | 'UPI' | null;
}

// types/auth.ts
export interface OtpVerifyResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  phoneNumber: string;
  name: string | null;
  requiresProfile: boolean;
}

// types/games.ts
export interface MyGamesResponse {
  games: GameSummary[];
  totalCount: number;
  upcomingCount: number;
  completedCount: number;
  cancelledCount: number;
}

export interface GameSummary {
  matchId: string;
  teamName: string;
  eventType: EventType;
  status: MatchStatus;
  startTime: string;
  groundMapsUrl: string;
  feePerPerson: number;
  userRole: 'CAPTAIN' | ParticipantRole;
  isCaptain: boolean;
  teamCount: number;
  backupCount: number;
  emergencyCount: number;
  requiredPlayers: number;
  backupSlots: number;
  paymentStatus: PaymentStatus | null;
  feeAmount: number | null;
}
```

### API Service Layer

```typescript
// services/api.ts
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

class ApiService {
  private getHeaders(auth: boolean = false): Headers {
    const headers = new Headers({
      'Content-Type': 'application/json',
    });
    
    if (auth) {
      const token = this.getAccessToken();
      if (token) {
        headers.append('Authorization', `Bearer ${token}`);
      }
    }
    
    return headers;
  }

  private getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  // Invite APIs
  async resolveInvite(token: string): Promise<InviteResponse> {
    const response = await fetch(
      `${API_BASE_URL}/v2/mvp/invites/${token}`,
      { headers: this.getHeaders(false) }
    );
    
    if (!response.ok) {
      const error = await response.json();
      throw new ApiError(error.code, error.detail, response.status);
    }
    
    return response.json();
  }

  // Auth APIs
  async requestOtp(phoneNumber: string): Promise<void> {
    const response = await fetch(
      `${API_BASE_URL}/v2/mvp/auth/otp/request`,
      {
        method: 'POST',
        headers: this.getHeaders(false),
        body: JSON.stringify({ phoneNumber }),
      }
    );
    
    if (!response.ok) {
      const error = await response.json();
      throw new ApiError(error.code, error.detail, response.status);
    }
  }

  async verifyOtp(phoneNumber: string, otpCode: string): Promise<OtpVerifyResponse> {
    const response = await fetch(
      `${API_BASE_URL}/v2/mvp/auth/otp/verify`,
      {
        method: 'POST',
        headers: this.getHeaders(false),
        body: JSON.stringify({ phoneNumber, otpCode }),
      }
    );
    
    if (!response.ok) {
      const error = await response.json();
      throw new ApiError(error.code, error.detail, response.status);
    }
    
    return response.json();
  }

  async updateProfile(name: string, area?: string): Promise<void> {
    const response = await fetch(
      `${API_BASE_URL}/v2/mvp/auth/profile`,
      {
        method: 'POST',
        headers: this.getHeaders(true),
        body: JSON.stringify({ name, area }),
      }
    );
    
    if (!response.ok) {
      const error = await response.json();
      throw new ApiError(error.code, error.detail, response.status);
    }
  }

  // Match APIs
  async getMatch(matchId: string): Promise<MatchResponse> {
    const response = await fetch(
      `${API_BASE_URL}/v2/mvp/matches/${matchId}`,
      { headers: this.getHeaders(true) }
    );
    
    if (!response.ok) {
      const error = await response.json();
      throw new ApiError(error.code, error.detail, response.status);
    }
    
    return response.json();
  }

  async respondToMatch(matchId: string, response: 'YES' | 'NO'): Promise<void> {
    const res = await fetch(
      `${API_BASE_URL}/v2/mvp/matches/${matchId}/respond`,
      {
        method: 'POST',
        headers: this.getHeaders(true),
        body: JSON.stringify({ response }),
      }
    );
    
    if (!res.ok) {
      const error = await res.json();
      throw new ApiError(error.code, error.detail, res.status);
    }
  }

  async getMyGames(): Promise<MyGamesResponse> {
    const response = await fetch(
      `${API_BASE_URL}/v2/mvp/matches/my-games`,
      { headers: this.getHeaders(true) }
    );
    
    if (!response.ok) {
      const error = await response.json();
      throw new ApiError(error.code, error.detail, response.status);
    }
    
    return response.json();
  }
}

// Custom error class
class ApiError extends Error {
  constructor(
    public code: string,
    public detail: string,
    public status: number
  ) {
    super(detail);
    this.name = 'ApiError';
  }
}

export const api = new ApiService();
```

---

## 5. State Management

### Auth Context

```typescript
// context/AuthContext.tsx
interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: {
    userId: number;
    phoneNumber: string;
    name: string | null;
  } | null;
}

const AuthContext = createContext<AuthState & AuthActions>(null!);

export const AuthProvider = ({ children }) => {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: false,
    isLoading: true,
    user: null,
  });

  useEffect(() => {
    // Check for existing tokens on mount
    const token = localStorage.getItem('accessToken');
    if (token) {
      // Validate token and restore user
      validateAndRestoreSession(token);
    } else {
      setState(prev => ({ ...prev, isLoading: false }));
    }
  }, []);

  const login = async (otpResponse: OtpVerifyResponse) => {
    localStorage.setItem('accessToken', otpResponse.accessToken);
    localStorage.setItem('refreshToken', otpResponse.refreshToken);
    
    setState({
      isAuthenticated: true,
      isLoading: false,
      user: {
        userId: otpResponse.userId,
        phoneNumber: otpResponse.phoneNumber,
        name: otpResponse.name,
      },
    });
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    
    setState({
      isAuthenticated: false,
      isLoading: false,
      user: null,
    });
  };

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### Invite State Hook

```typescript
// hooks/useInvite.ts
export const useInvite = (token: string) => {
  const [state, setState] = useState({
    inviteData: null as InviteResponse | null,
    loading: true,
    error: null as string | null,
  });

  useEffect(() => {
    const fetchInvite = async () => {
      try {
        const data = await api.resolveInvite(token);
        setState({ inviteData: data, loading: false, error: null });
      } catch (err) {
        setState({
          inviteData: null,
          loading: false,
          error: err instanceof ApiError ? err.detail : 'Failed to load invite',
        });
      }
    };

    fetchInvite();
  }, [token]);

  return state;
};
```

---

## 6. Error Handling

### Error Mapping

```typescript
// utils/errorMessages.ts
const errorMessages: Record<string, string> = {
  // Invite errors
  'MVP-INVITE-001': 'This invite link is invalid or has been deleted.',
  'MVP-INVITE-002': 'This invite link has expired.',
  
  // Auth errors
  'MVP-AUTH-001': 'The OTP you entered is incorrect. Please try again.',
  'MVP-AUTH-002': 'Your OTP has expired. Please request a new one.',
  'MVP-AUTH-003': 'Too many failed attempts. Please request a new OTP.',
  'MVP-AUTH-004': 'Too many OTP requests. Please wait 10 minutes.',
  'MVP-AUTH-011': 'Please login to continue.',
  
  // Match errors
  'MVP-MATCH-001': 'Match not found.',
  'MVP-MATCH-002': 'This match is full. Try requesting an emergency spot.',
  'MVP-MATCH-005': 'This match is no longer accepting responses.',
};

export const getUserFriendlyError = (code: string, fallback: string): string => {
  return errorMessages[code] || fallback;
};
```

### Error Boundary Component

```typescript
// components/ErrorBoundary.tsx
const ErrorBoundary = ({ children }) => {
  const [hasError, setHasError] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  if (hasError) {
    return (
      <div className="error-screen">
        <h1>Something went wrong</h1>
        <p>{error?.message}</p>
        <button onClick={() => window.location.reload()}>
          Try Again
        </button>
      </div>
    );
  }

  return children;
};
```

---

## 7. Testing Checklist

### Invite Flow Testing

- [ ] Open valid team invite link → Shows match preview
- [ ] Open valid emergency invite link → Shows emergency badge
- [ ] Open invalid/expired invite → Shows error message
- [ ] Click "I'm In!" when not logged in → Redirects to OTP screen
- [ ] Complete OTP flow → Returns to invite and submits response
- [ ] Click "I'm In!" when logged in → Submits response directly
- [ ] Respond YES to full match → Shows "match full" error
- [ ] Respond NO → Shows unavailable confirmation

### Auth Flow Testing

- [ ] Request OTP with valid phone → Success (204)
- [ ] Request OTP with invalid phone → Shows validation error
- [ ] Verify with correct OTP (123456) → Returns tokens
- [ ] Verify with wrong OTP → Shows error with remaining attempts
- [ ] New user verification → `requiresProfile: true`, redirect to profile
- [ ] Existing user verification → `requiresProfile: false`, proceed

### Match Details Testing

- [ ] View match as captain → Shows full participant list
- [ ] View match as participant → Shows counts only
- [ ] View match as visitor → Shows public info only

### Edge Cases

- [ ] Token expiry during flow → Redirect to login
- [ ] Network error → Show retry option
- [ ] Concurrent response (match fills while viewing) → Show appropriate error
- [ ] Browser back/forward navigation → Maintain state correctly

---

## Summary

### Key URLs for Frontend

| Route | Purpose |
|-------|---------|
| `/invite/:token` | Handle invite links |
| `/auth/login` | OTP login screen |
| `/auth/profile` | Profile setup (new users) |
| `/match/:matchId` | Match details view |
| `/match/:matchId/confirmed` | Response confirmation |
| `/my-games` | User's games list |

### API Call Sequence

1. **Resolve Invite:** `GET /v2/mvp/invites/{token}` (no auth)
2. **Auth (if needed):**
   - `POST /v2/mvp/auth/otp/request`
   - `POST /v2/mvp/auth/otp/verify`
   - `POST /v2/mvp/auth/profile` (if requiresProfile)
3. **Respond:** `POST /v2/mvp/matches/{matchId}/respond`
4. **Get Details:** `GET /v2/mvp/matches/{matchId}`

### Important Notes

1. **Invite resolution is public** - No auth required
2. **Response requires auth** - Store pending action during auth flow
3. **OTP is hardcoded** - Use `123456` for MVP testing
4. **Role assignment is automatic** - Backend assigns TEAM → BACKUP based on availability
5. **Idempotent responses** - Multiple YES calls update same record

---

**End of Document**

Happy coding! 🚀

