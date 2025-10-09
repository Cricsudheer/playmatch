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

## Expected Responses:

### Successful Registration
```json
{
  "id": "uuid",
  "email": "test@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2025-09-27T14:00:00Z"
}
```

### Successful Login
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "tokenType": "Bearer"
}
```

### Successful Password Reset Request
```json
{
  "message": "If an account exists, instructions were sent."
}
```

### Successful Password Reset
```json
{
  "message": "Password updated successfully."
}
```
