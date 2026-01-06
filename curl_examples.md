# PlayMatch API - cURL Examples

## Register New User
```bash
curl -X POST http://localhost:8081/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!",
    "reEnterPassword": "Password123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

## Login
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

## Access Protected Resource (using JWT)
```bash
curl -X GET http://localhost:8081/v1/examples \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Forgot Password
```bash
curl -X POST http://localhost:8081/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

## Reset Password (with token)
```bash
curl -X POST http://localhost:8081/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_RESET_TOKEN",
    "password": "NewPassword123!",
    "reEnterPassword": "NewPassword123!"
  }'
```

## Testing Notes:
1. The server runs on port 8081
2. All endpoints use JSON for request/response
3. JWT tokens are returned in the login response
4. Use the JWT token in the Authorization header for protected endpoints
5. All passwords must meet security requirements (uppercase, lowercase, number, special char)

## Sigma Module - Player Stats Endpoints

### Get Player Stats (Batting, Bowling, Dismissal)
```bash
curl -X GET http://localhost:8080/api/players/1/stats \
  -H "Content-Type: application/json"
```

**Example Response (200 OK):**
```json
{
  "playerId": 1,
  "playerName": "Virat Kohli",
  "battingStats": [
    {
      "id": 1,
      "player": {
        "id": 1,
        "name": "Virat Kohli"
      },
      "innings": 120,
      "averageScore": 54.50,
      "runsScored": 6540,
      "createdAt": "2025-01-01T10:00:00Z",
      "updatedAt": "2025-01-06T14:30:00Z"
    }
  ],
  "bowlingStats": [
    {
      "id": 1,
      "player": {
        "id": 1,
        "name": "Virat Kohli"
      },
      "innings": 45,
      "wicketsTaken": 12,
      "economyRate": 5.25,
      "createdAt": "2025-01-01T10:00:00Z",
      "updatedAt": "2025-01-06T14:30:00Z"
    }
  ],
  "dismissalStats": [
    {
      "id": 1,
      "player": {
        "id": 1,
        "name": "Virat Kohli"
      },
      "innings": 120,
      "dismissals": 18,
      "createdAt": "2025-01-01T10:00:00Z",
      "updatedAt": "2025-01-06T14:30:00Z"
    }
  ]
}
```

**Error Response (404 Not Found):**
```json
{}
```

### Get Player Information
```bash
curl -X GET http://localhost:8080/api/players/1 \
  -H "Content-Type: application/json"
```

**Example Response (200 OK):**
```json
{
  "id": 1,
  "name": "Virat Kohli",
  "createdAt": "2025-01-01T10:00:00Z",
  "updatedAt": "2025-01-06T14:30:00Z"
}
```

**Error Response (404 Not Found):**
```json
{}
```

### Get All Players Stats (Optimized with Inner Joins)
```bash
curl -X GET http://localhost:8080/sigma/api/players/all/stats \
  -H "Content-Type: application/json"
```

**Example Response (200 OK):**
```json
[
  {
    "playerId": 1,
    "playerName": "Virat Kohli",
    "battingStats": {
      "id": 1,
      "player": {
        "id": 1,
        "name": "Virat Kohli"
      },
      "innings": 120,
      "averageScore": 54.50,
      "runsScored": 6540,
      "createdAt": "2025-01-01T10:00:00Z",
      "updatedAt": "2025-01-06T14:30:00Z"
    },
    "bowlingStats": {
      "id": 1,
      "player": {
        "id": 1,
        "name": "Virat Kohli"
      },
      "innings": 45,
      "wicketsTaken": 12,
      "economyRate": 5.25,
      "createdAt": "2025-01-01T10:00:00Z",
      "updatedAt": "2025-01-06T14:30:00Z"
    },
    "dismissalStats": {
      "id": 1,
      "player": {
        "id": 1,
        "name": "Virat Kohli"
      },
      "innings": 120,
      "dismissals": 18,
      "createdAt": "2025-01-01T10:00:00Z",
      "updatedAt": "2025-01-06T14:30:00Z"
    }
  },
  {
    "playerId": 2,
    "playerName": "Rohit Sharma",
    "battingStats": {
      "id": 2,
      "player": {
        "id": 2,
        "name": "Rohit Sharma"
      },
      "innings": 115,
      "averageScore": 52.30,
      "runsScored": 6015,
      "createdAt": "2025-01-02T10:00:00Z",
      "updatedAt": "2025-01-06T14:30:00Z"
    },
    "bowlingStats": null,
    "dismissalStats": {
      "id": 2,
      "player": {
        "id": 2,
        "name": "Rohit Sharma"
      },
      "innings": 115,
      "dismissals": 22,
      "createdAt": "2025-01-02T10:00:00Z",
      "updatedAt": "2025-01-06T14:30:00Z"
    }
  }
]
```

**Performance Benefits:**
- Uses `INNER JOIN FETCH` to eagerly load player data with their stats
- Reduces N+1 query problems
- Includes player names in response without additional queries
- Optimized for bulk data retrieval

## Sigma Endpoints Testing Script

You can use this bash script to test all sigma endpoints:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api/players"
PLAYER_ID=1

echo "========== Testing Sigma Endpoints =========="

echo -e "\n1. Get Player Information"
curl -X GET "$BASE_URL/$PLAYER_ID" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"

echo -e "\n2. Get Player Stats (Batting, Bowling, Dismissal)"
curl -X GET "$BASE_URL/$PLAYER_ID/stats" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"

echo -e "\n3. Get Non-existent Player (Should return 404)"
curl -X GET "$BASE_URL/99999" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"

echo -e "\n========== Test Complete =========="
```

## Sigma Endpoints Summary

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/api/players/{playerId}` | Get player information | 200, 404, 500 |
| GET | `/api/players/{playerId}/stats` | Get all player stats (batting, bowling, dismissal) | 200, 404, 500 |

## Quick Reference - Common cURL Flags

```bash
# Pretty print JSON response
curl -X GET http://localhost:8080/api/players/1 | jq .

# Show response headers
curl -i http://localhost:8080/api/players/1

# Follow redirects
curl -L http://localhost:8080/api/players/1

# Set custom timeout (seconds)
curl --max-time 10 http://localhost:8080/api/players/1

# Save response to file
curl http://localhost:8080/api/players/1 > response.json

# Show verbose output
curl -v http://localhost:8080/api/players/1
```
