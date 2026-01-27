# GameTeam MVP - Postman Testing Guide

## 📦 Files Created

1. **GameTeam_MVP.postman_collection.json** - Complete API collection with 25+ requests
2. **GameTeam_MVP.postman_environment.json** - Environment variables for local testing

## 🚀 Quick Start

### 1. Import into Postman

**Import Collection:**
1. Open Postman
2. Click **Import** button (top left)
3. Drag and drop `GameTeam_MVP.postman_collection.json`
4. Click **Import**

**Import Environment:**
1. Click **Import** again
2. Drag and drop `GameTeam_MVP.postman_environment.json`
3. Click **Import**

**Activate Environment:**
1. Click the environment dropdown (top right)
2. Select **"GameTeam MVP - Local"**

### 2. Start Your Application

```bash
# From project root
mvn spring-boot:run

# Or if using Docker
docker-compose up
```

Wait for the application to start on `http://localhost:8080`

### 3. Run Your First Test

1. Open the collection: **GameTeam MVP API**
2. Navigate to: **1. Authentication → Request OTP**
3. Click **Send**
4. Check the console - you'll see: `✅ OTP sent! Use hardcoded OTP: 123456`

## 📚 Collection Structure

### 1. Authentication (3 requests)
- **Request OTP** - Request OTP for phone number
- **Verify OTP** - Verify with hardcoded "123456", get JWT tokens
- **Update Profile** - Set user name and area

### 2. Match Management (7 requests)
- **Create Match** - Create new match as captain
- **Get Match Details** - View match (captain sees full details)
- **Respond YES** - Confirm participation
- **Respond NO** - Mark unavailable
- **Complete Match** - Mark as completed (captain only)
- **Cancel Match** - Cancel match (captain only)
- **Get My Games** - Retrieve all matches where user is captain or participant

### 3. Invites (2 requests)
- **Resolve Team Invite** - PUBLIC endpoint to view match
- **Resolve Emergency Invite** - PUBLIC emergency invite

### 4. Emergency Requests (4 requests)
- **Request Emergency Spot** - Request to play as emergency
- **Get Pending Requests** - Captain views pending requests
- **Approve Request** - Captain approves emergency player
- **Reject Request** - Captain rejects emergency player

### 5. Backout Tracking (1 request)
- **Log Backout** - Captain logs backout with reason

### 6. Payment Tracking (1 request)
- **Mark Payment** - Captain marks payment as paid

### 7. Complete User Journey (4 requests)
- Full multi-user flow with second user

## 🎯 Testing Workflows

### Workflow 1: Single User - Create & Complete Match

**Run in order:**

1. **1. Authentication → Request OTP**
   - Phone: `+919876543210` (default)
   - ✅ Check console for "OTP sent"

2. **1. Authentication → Verify OTP**
   - OTP: `123456` (hardcoded for MVP)
   - ✅ Saves `access_token` and `user_id` automatically

3. **1. Authentication → Update Profile**
   - Sets name: "Rahul Sharma"
   - Sets area: "Koramangala"

4. **2. Match Management → Create Match**
   - Creates match with all details
   - ✅ Saves `match_id` and invite URLs automatically
   - ✅ Check response for invite URLs

5. **2. Match Management → Get Match Details**
   - View match as captain
   - ✅ See full participant list (captain view)

6. **2. Match Management → Complete Match**
   - Mark match as completed
   - ✅ Records ₹50 platform fee

**Expected Result:** Match created, viewed, and completed successfully!

---

### Workflow 1.5: View My Games

**Run after creating/responding to matches:**

1. **2. Match Management → Get My Games**
   - ✅ See all matches where you're captain or participant
   - ✅ Check totalCount, upcomingCount, completedCount stats
   - ✅ See your role in each match (CAPTAIN/TEAM/BACKUP/EMERGENCY)
   - ✅ View payment status for participant matches
   - ✅ Sorted by most recent matches first

**Expected Result:** List of all user's matches with role and stats!

---

### Workflow 2: Multi-User - Full Match Flow

**Run in order:**

1. **Setup User 1 (Captain)**
   - Run: `1. Authentication → Request OTP`
   - Run: `1. Authentication → Verify OTP`
   - Run: `1. Authentication → Update Profile`
   - Run: `2. Match Management → Create Match`

2. **Setup User 2 (Player)**
   - Run: `7. Complete User Journey → Setup → Request OTP (User 2)`
   - Run: `7. Complete User Journey → Setup → Verify OTP (User 2)`
   - Run: `7. Complete User Journey → Setup → Update Profile (User 2)`
   - ✅ User 2 token saved as `access_token_2`

3. **User 2 Joins Match**
   - Run: `7. Complete User Journey → User 2: Respond to Match`
   - ✅ User 2 confirmed for match

4. **Captain Views Participants**
   - Run: `2. Match Management → Get Match Details`
   - ✅ See both captain and User 2 in participants list

5. **Captain Marks Payment**
   - Run: `7. Complete User Journey → Captain: Mark User 2 Payment`
   - ✅ Payment marked as PAID/UPI

6. **View My Games (Both Users)**
   - User 1: Run `2. Match Management → Get My Games`
     - ✅ See match with userRole: "CAPTAIN", isCaptain: true
   - User 2: Switch to `{{access_token_2}}`, run `Get My Games`
     - ✅ See match with userRole: "TEAM", paymentStatus: "PAID"

**Expected Result:** Two-user match with payment tracking!

---

### Workflow 3: Emergency Player Flow

**Prerequisites:** Run Workflow 1 steps 1-4 (create match with emergency enabled)

**Run in order:**

1. **Setup User 2 (Emergency Player)**
   - Run: `7. Complete User Journey → Setup → Request OTP (User 2)`
   - Run: `7. Complete User Journey → Setup → Verify OTP (User 2)`
   - Run: `7. Complete User Journey → Setup → Update Profile (User 2)`

2. **User 2 Requests Emergency Spot**
   - Run: `4. Emergency Requests → Request Emergency Spot`
   - Switch to User 2 token first!
   - ✅ Emergency request created with 60-min lock

3. **Captain Views Pending Requests**
   - Switch back to captain token (`access_token`)
   - Run: `4. Emergency Requests → Get Pending Emergency Requests`
   - ✅ See User 2's request with trust score and area
   - ✅ Request ID saved automatically

4. **Captain Approves Request**
   - Run: `4. Emergency Requests → Approve Emergency Request`
   - ✅ User 2 added as EMERGENCY participant

5. **Verify Participant Added**
   - Run: `2. Match Management → Get Match Details`
   - ✅ See User 2 with role=EMERGENCY

**Expected Result:** Emergency player approved and added to match!

---

### Workflow 4: Public Invite Resolution

**Prerequisites:** Create a match (Workflow 1 steps 1-4)

**No Authentication Required!**

1. **Resolve Team Invite**
   - Run: `3. Invites → Resolve Team Invite (Public)`
   - ✅ Returns match details WITHOUT authentication
   - ✅ Shows team name, ground, start time

2. **Resolve Emergency Invite**
   - Run: `3. Invites → Resolve Emergency Invite (Public)`
   - ✅ Returns emergency invite details

**Expected Result:** Public can view match details via invite links!

---

## 🔧 Environment Variables

**Automatically Set:**
- `access_token` - JWT token for User 1 (captain)
- `access_token_2` - JWT token for User 2
- `user_id` - User 1 ID
- `user_id_2` - User 2 ID
- `match_id` - Created match ID
- `team_invite_token` - Team invite token
- `emergency_invite_token` - Emergency invite token
- `emergency_request_id` - Emergency request ID

**Manually Editable:**
- `base_url` - API base URL (default: `http://localhost:8080`)
- `phone_number` - User 1 phone (default: `+919876543210`)
- `phone_number_2` - User 2 phone (default: `+919876543211`)

## 📝 Request Features

### Automatic Token Management
All authenticated requests use `{{access_token}}` variable.
Token is automatically saved after OTP verification!

### Console Logging
Every request logs helpful information:
- ✅ Success messages
- Match details
- User IDs
- Invite URLs
- Request status

### Auto-Save Important Data
Scripts automatically save:
- JWT tokens
- User IDs
- Match IDs
- Invite tokens
- Request IDs

### Test Assertions
Each request includes tests:
- Status code verification
- Response validation
- Data extraction

## 🎨 Tips & Tricks

### 1. Switch Between Users
To test as different users:
1. Copy `access_token` value
2. Paste into `access_token_2`
3. Or manually change Authorization header

### 2. View Console Output
- Open Postman Console (bottom left)
- See detailed logs for each request
- Debug issues easily

### 3. Test Scenarios

**Test Rate Limiting:**
- Run `Request OTP` 4 times quickly
- 4th request should return `429 Too Many Requests`

**Test Max OTP Attempts:**
- Request OTP
- Verify with wrong code 6 times
- Should get "Maximum attempts exceeded"

**Test Emergency Lock Expiry:**
- Create emergency request
- Wait 60+ minutes
- Scheduler will auto-expire (runs every 5 min)

**Test Captain Authorization:**
- Create match with User 1
- Try to complete match with User 2 token
- Should get `403 Forbidden - Not Captain`

### 4. Error Handling

All errors return structured responses:
```json
{
  "code": "MVP-MATCH-001",
  "title": "Match not found",
  "status": 404,
  "detail": "Detailed error message"
}
```

### 5. Test Idempotency

**YES Response:**
- Run `Respond YES` twice
- Should update same participant record

**Match Creation:**
- Same match ID used throughout session
- Stored in `{{match_id}}` variable

## 🐛 Troubleshooting

### Issue: "Cannot find match"
**Solution:** Run `Create Match` first to generate `match_id`

### Issue: "Unauthorized"
**Solution:** Run `Request OTP` → `Verify OTP` to get fresh token

### Issue: "Not Captain"
**Solution:** Switch to captain's `access_token` (User 1)

### Issue: "Rate limit exceeded"
**Solution:** Wait 10 minutes or use different phone number

### Issue: "Emergency not enabled"
**Solution:** Ensure `emergencyEnabled: true` in Create Match request

### Issue: "Invalid OTP"
**Solution:** Always use hardcoded OTP: `123456`

## 📊 Database Verification

After running tests, check database:

```sql
-- View all MVP users
SELECT * FROM mvp_user ORDER BY created_at DESC;

-- View matches with participants
SELECT
    m.team_name,
    m.status,
    COUNT(mp.id) as participant_count
FROM match m
LEFT JOIN match_participant mp ON m.id = mp.match_id
GROUP BY m.id, m.team_name, m.status;

-- View emergency requests
SELECT
    er.status,
    u.name,
    er.requested_at,
    er.lock_expires_at
FROM emergency_request er
JOIN mvp_user u ON er.user_id = u.id
ORDER BY er.created_at DESC;

-- View payment status
SELECT
    u.name,
    mp.payment_status,
    mp.payment_mode,
    mp.fee_amount
FROM match_participant mp
JOIN mvp_user u ON mp.user_id = u.id
ORDER BY mp.updated_at DESC;
```

## 🎯 Complete Test Checklist

- [ ] OTP Request & Verify
- [ ] Profile Update
- [ ] Match Creation
- [ ] Match Details (Captain View)
- [ ] YES Response (Idempotent)
- [ ] NO Response
- [ ] Public Invite Resolution
- [ ] Emergency Request
- [ ] Captain Approve Emergency
- [ ] Captain Reject Emergency
- [ ] Payment Marking
- [ ] Backout Logging
- [ ] Match Completion
- [ ] Match Cancellation
- [ ] Rate Limiting (3 OTP per 10 min)
- [ ] Max Attempts (5 OTP tries)
- [ ] Multi-User Flow
- [ ] Captain Authorization
- [ ] Role-Aware Views

## 📞 Support

**Check Application Logs:**
```bash
# View real-time logs
mvn spring-boot:run

# Check for:
# - OTP codes in console
# - Emergency lock expiry scheduler
# - Match creation logs
# - Authorization errors
```

**Common Log Messages:**
- `OTP for +919876543210: 123456 (HARDCODED - MVP MODE)`
- `Match created: id=xxx, captain=1, team=Koramangala Knights`
- `Emergency request created: matchId=xxx, userId=2`
- `Expired 1 emergency request(s)`

## 🎉 You're Ready!

Import the collection, activate the environment, and start testing!

**Recommended First Run:**
1. Run Workflow 1 (Single User)
2. Then Workflow 2 (Multi-User)
3. Finally Workflow 3 (Emergency Flow)

Happy Testing! 🚀
