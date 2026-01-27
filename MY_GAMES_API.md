# My Games API - Quick Reference

## Overview
The My Games API provides a unified view of all matches where the current user is involved, either as a captain or participant.

## Endpoint

```
GET /v2/mvp/matches/my-games
```

**Authentication:** Required (JWT Bearer token)

## Response Structure

```json
{
  "games": [
    {
      "matchId": "uuid",
      "teamName": "string",
      "status": "CREATED|ACTIVE|COMPLETED|CANCELLED",
      "startTime": "ISO8601",
      "userRole": "CAPTAIN|TEAM|BACKUP|EMERGENCY",
      "isCaptain": boolean,
      "teamCount": number,
      "backupCount": number,
      "emergencyCount": number,
      "paymentStatus": "PAID|UNPAID|null",
      "paymentMode": "CASH|UPI|null",
      "feeAmount": number|null,
      ...
    }
  ],
  "totalCount": number,
  "upcomingCount": number,
  "completedCount": number,
  "cancelledCount": number
}
```

## Use Cases

### 1. Dashboard/Home Screen
Display user's matches grouped by status:
- **Upcoming:** CREATED or ACTIVE matches
- **Past:** COMPLETED matches
- **Cancelled:** CANCELLED matches

### 2. Match History
Show chronological list of all user's matches with:
- User's role in each match (Captain/Team/Backup/Emergency)
- Payment status (for participant matches)
- Quick navigation to match details

### 3. Payment Tracking
Filter and display matches where user owes payment:
```javascript
const unpaidMatches = response.games.filter(game =>
  !game.isCaptain && game.paymentStatus === 'UNPAID'
);
```

### 4. Captain Dashboard
Filter matches where user is captain:
```javascript
const captainMatches = response.games.filter(game => game.isCaptain);
```

## Key Features

✅ **Unified View** - Single endpoint for all user matches
✅ **Role-Aware** - Clearly indicates user's role in each match
✅ **Payment Info** - Shows payment status for participant matches
✅ **Smart Sorting** - Pre-sorted by start time (most recent first)
✅ **Summary Stats** - Aggregated counts for quick insights
✅ **Complete Data** - Includes all match details needed for UI

## Example cURL Request

```bash
curl -X GET http://localhost:8080/v2/mvp/matches/my-games \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Frontend Integration

### React Example

```typescript
const MyGamesScreen = () => {
  const { data, isLoading } = useQuery('myGames', async () => {
    const response = await fetch('/v2/mvp/matches/my-games', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    return response.json();
  });

  if (isLoading) return <Loading />;

  return (
    <div>
      <Summary>
        <Stat label="Total" value={data.totalCount} />
        <Stat label="Upcoming" value={data.upcomingCount} />
        <Stat label="Completed" value={data.completedCount} />
      </Summary>

      <MatchList>
        {data.games.map(game => (
          <MatchCard
            key={game.matchId}
            match={game}
            role={game.userRole}
            isCaptain={game.isCaptain}
          />
        ))}
      </MatchList>
    </div>
  );
};
```

### Filtering Examples

```typescript
// Upcoming matches only
const upcoming = data.games.filter(g =>
  g.status === 'CREATED' || g.status === 'ACTIVE'
);

// Captain matches
const asCapt = data.games.filter(g => g.isCaptain);

// Participant matches
const asPlayer = data.games.filter(g => !g.isCaptain);

// Unpaid matches
const unpaid = data.games.filter(g =>
  !g.isCaptain && g.paymentStatus === 'UNPAID'
);

// By date range
const thisWeek = data.games.filter(g => {
  const start = new Date(g.startTime);
  const now = new Date();
  const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
  return start >= weekAgo && start <= now;
});
```

## Testing

### Postman Request

1. Set URL: `GET http://localhost:8080/v2/mvp/matches/my-games`
2. Add Header: `Authorization: Bearer {{access_token}}`
3. Send request

Expected response contains user's matches with proper role assignment.

### Test Scenarios

1. **New User (No Games)**
   - Response: `totalCount: 0`, `games: []`
   - Empty state UI should display

2. **Captain Only**
   - All matches have `isCaptain: true`
   - Payment fields are `null`

3. **Participant Only**
   - All matches have `isCaptain: false`
   - Payment fields populated

4. **Mixed Roles**
   - Some matches as captain, some as participant
   - Payment fields conditional on role

## Performance Considerations

- **Caching:** Cache response locally, invalidate on:
  - Match creation
  - Match response (YES/NO)
  - Payment updates

- **Pagination:** Not implemented in MVP, all matches returned

- **Sorting:** Pre-sorted by backend (startTime DESC)

## Related Endpoints

- `GET /v2/mvp/matches/{id}` - View specific match details
- `POST /v2/mvp/matches` - Create new match (adds to myGames)
- `POST /v2/mvp/matches/{id}/respond` - Respond to match (updates myGames)

## Error Handling

```typescript
try {
  const response = await fetch('/v2/mvp/matches/my-games', {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (response.status === 401) {
    // Token expired or invalid
    redirectToLogin();
  }

  const data = await response.json();
  return data;
} catch (error) {
  // Network error
  showError('Unable to load matches. Please try again.');
}
```

## Migration Notes

If upgrading from a previous version without myGames:
- This is a NEW endpoint (no breaking changes)
- Can replace multiple separate API calls for:
  - Captain's matches: `GET /matches?createdBy={userId}`
  - User's participations: Multiple queries per match
- Single source of truth for "user's games"

---

**Version:** 1.0
**Last Updated:** January 23, 2026
**API Prefix:** `/v2/mvp`
