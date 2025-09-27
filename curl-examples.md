# PlayMatch API cURL Examples

## User Registration
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"Password123!\",\"firstName\":\"Test\",\"lastName\":\"User\"}"
```

## Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"Password123!\"}"
```

## Get Protected Resource (with JWT)
```bash
curl -X GET http://localhost:8081/api/examples \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Forgot Password
```bash
curl -X POST http://localhost:8081/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\"}"
```

## Reset Password (with token)
```bash
curl -X POST http://localhost:8081/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"YOUR_RESET_TOKEN\",\"newPassword\":\"NewPassword123!\"}"
```
