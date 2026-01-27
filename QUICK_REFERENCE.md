# GameTeam MVP - Quick Reference Card

## 🔐 Hardcoded Values (MVP Testing)

| Item | Value |
|------|-------|
| **OTP Code** | `123456` |
| **User 1 Phone** | `+919876543210` |
| **User 2 Phone** | `+919876543211` |
| **Platform Fee** | ₹50 |
| **OTP Expiry** | 5 minutes |
| **Max OTP Attempts** | 5 |
| **Rate Limit** | 3 requests per 10 min |
| **Emergency Lock** | 60 minutes |

## 📡 API Endpoints Summary

### Public (No Auth Required)
```
GET  /v2/mvp/invites/{token}           # Resolve invite
POST /v2/mvp/auth/otp/request          # Request OTP
POST /v2/mvp/auth/otp/verify           # Verify OTP
```

### Authenticated
```
POST /v2/mvp/auth/profile              # Update profile
POST /v2/mvp/matches                   # Create match
GET  /v2/mvp/matches/{id}              # View match
POST /v2/mvp/matches/{id}/respond      # YES/NO
GET  /v2/mvp/matches/my-games          # Get user's matches
```

### Captain Only
```
POST /v2/mvp/matches/{id}/complete     # Complete match
POST /v2/mvp/matches/{id}/cancel       # Cancel match
POST /v2/mvp/matches/{id}/backout      # Log backout
POST /v2/mvp/matches/{id}/payments/mark # Mark payment
GET  /v2/mvp/matches/{id}/emergency/requests  # View requests
POST /v2/mvp/matches/{id}/emergency/{rid}/approve
POST /v2/mvp/matches/{id}/emergency/{rid}/reject
```

## 🎯 Quick Test Commands

### 1. Get JWT Token
```bash
# Request OTP
curl -X POST http://localhost:8080/v2/mvp/auth/otp/request \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+919876543210"}'

# Verify OTP
curl -X POST http://localhost:8080/v2/mvp/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+919876543210", "otpCode": "123456"}'
```

### 2. Create Match
```bash
curl -X POST http://localhost:8080/v2/mvp/matches \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "teamName": "Knights",
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
  }'
```

## 🗂️ Database Tables

1. **mvp_user** - Phone-based users
2. **otp_verification** - OTP codes
3. **otp_rate_limit** - Rate limiting
4. **match** - Match details
5. **match_invite** - Invite tokens
6. **match_participant** - YES responses
7. **match_unavailability** - NO responses
8. **emergency_request** - Emergency requests
9. **emergency_pool** - Emergency player pool
10. **backout_log** - Backout tracking
11. **platform_fee_log** - Platform fees

## 🎭 User Roles (Per Match)

| Role | Definition |
|------|------------|
| **Captain** | `match.created_by` |
| **Player** | User who responds YES |
| **Emergency** | Approved emergency request |

### Participant Sub-Roles
- **TEAM** - First N players (requiredPlayers)
- **BACKUP** - Next M players (backupSlots)
- **EMERGENCY** - Emergency players (unlimited)

## 📊 Enums

### EventType
- `PRACTICE`
- `TOURNAMENT`
- `NETS`

### BallCategory
- `LEATHER`
- `TENNIS`

### BallVariant (Leather)
- `WHITE`
- `RED`
- `PINK`

### BallVariant (Tennis)
- `HARD`
- `SOFT`

### MatchStatus
- `CREATED`
- `ACTIVE`
- `COMPLETED`
- `CANCELLED`

### ParticipantStatus
- `CONFIRMED`
- `BACKED_OUT`
- `NO_SHOW`

### PaymentStatus
- `PAID`
- `UNPAID`

### PaymentMode
- `CASH`
- `UPI`

### BackoutReason
- `GENUINE`
- `CONFLICT`
- `COMMUNICATION`
- `PAYMENT`
- `NO_SHOW`
- `CAPTAIN_DECISION`

### EmergencyRequestStatus
- `REQUESTED`
- `APPROVED`
- `REJECTED`
- `EXPIRED`

## 🔍 Common Queries

### Find User by Phone
```sql
SELECT * FROM mvp_user WHERE phone_number = '+919876543210';
```

### Match with Participants
```sql
SELECT
    m.team_name,
    COUNT(mp.id) as total_participants,
    COUNT(CASE WHEN mp.role = 'TEAM' THEN 1 END) as team,
    COUNT(CASE WHEN mp.role = 'EMERGENCY' THEN 1 END) as emergency
FROM match m
LEFT JOIN match_participant mp ON m.id = mp.match_id
    AND mp.status = 'CONFIRMED'
GROUP BY m.id, m.team_name;
```

### Pending Emergency Requests
```sql
SELECT
    er.id,
    u.name,
    er.requested_at,
    er.lock_expires_at,
    er.status
FROM emergency_request er
JOIN mvp_user u ON er.user_id = u.id
WHERE er.status = 'REQUESTED'
ORDER BY er.requested_at;
```

## ⚠️ Error Codes

| Code | Meaning |
|------|---------|
| `MVP-AUTH-001` | Invalid OTP |
| `MVP-AUTH-002` | OTP expired |
| `MVP-AUTH-003` | Max attempts exceeded |
| `MVP-AUTH-004` | Rate limit exceeded |
| `MVP-MATCH-001` | Match not found |
| `MVP-MATCH-002` | Match full |
| `MVP-INVITE-001` | Invite not found |
| `MVP-EMERGENCY-002` | Already requested |
| `MVP-AUTH-010` | Not captain |

## 🚦 Status Codes

| Code | Meaning |
|------|---------|
| `200` | Success with body |
| `204` | Success (no content) |
| `400` | Bad request |
| `401` | Unauthorized |
| `403` | Forbidden (not captain) |
| `404` | Not found |
| `429` | Rate limit exceeded |

## 🎯 Testing Checklist

**Basic Flow:**
- [ ] Request OTP
- [ ] Verify OTP
- [ ] Update profile
- [ ] Create match
- [ ] View match
- [ ] Respond YES
- [ ] Complete match

**Advanced:**
- [ ] Multi-user flow
- [ ] Emergency requests
- [ ] Payment marking
- [ ] Backout logging
- [ ] Rate limiting
- [ ] Captain authorization

## 📞 Quick Tips

1. **Always use hardcoded OTP:** `123456`
2. **Check console logs** for OTP messages
3. **Scheduler runs every 5 minutes** for emergency expiry
4. **Captain = match.created_by** (not a global role)
5. **Invite tokens are 8 chars** (A-Z0-9)
6. **YES is idempotent** (safe to call twice)
7. **Platform fee = ₹50** per completed match

## 🔧 Environment Setup

```bash
# Start application
mvn spring-boot:run

# Check database
psql -U postgres -d playmatchdb

# View logs
tail -f logs/application.log

# Run tests (when available)
mvn test
```

## 📚 File Locations

```
playmatch/
├── GameTeam_MVP.postman_collection.json
├── GameTeam_MVP.postman_environment.json
├── POSTMAN_TESTING_GUIDE.md
├── QUICK_REFERENCE.md (this file)
└── src/main/java/com/example/playmatch/mvp/
    ├── auth/
    ├── matches/
    ├── invites/
    ├── emergency/
    ├── backout/
    ├── payments/
    └── common/
```

---

**Need help?** Check `POSTMAN_TESTING_GUIDE.md` for detailed workflows!
