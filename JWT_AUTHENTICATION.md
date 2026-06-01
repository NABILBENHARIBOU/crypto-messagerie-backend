# JWT Authentication API Documentation

## Overview
The backend now includes JWT (JSON Web Token) authentication using JJWT 0.12.3 library. This provides secure token-based authentication for the messaging application.

## Configuration

### JWT Properties (application.properties)
```properties
jwt.secret=messagerie-secure-chat-jwt-secret-key-2024-minimum-32-characters-required
jwt.expiration=86400000              # 24 hours in milliseconds
jwt.refresh-expiration=604800000     # 7 days in milliseconds
```

### Supported Profiles
- **default/dev**: Development environment
- **prod**: Production environment with environment variables

## Authentication Endpoints

### 1. Login
**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@example.com",
    "role": "USER",
    "twoFactorEnabled": false,
    "blocked": false
  },
  "twoFactorRequired": false,
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

### 2. Register
**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "newuser@example.com",
  "password": "securePassword123!",
  "phone": "+1234567890",
  "twoFactorEnabled": false
}
```

**Response (201 Created):** Same as login response

### 3. Refresh Token
**Endpoint:** `POST /api/auth/refresh`

**Request:**
```json
{
  "token": "eyJhbGc...refreshToken..."
}
```

**Response (200 OK):** Returns new access and refresh tokens

### 4. Validate Token
**Endpoint:** `POST /api/auth/validate`

**Request:**
```json
{
  "token": "eyJhbGc..."
}
```

**Response (200 OK):**
```json
{
  "valid": true,
  "email": "user@example.com",
  "role": "USER"
}
```

## Using JWT Tokens

### In HTTP Requests
Add the access token to the `Authorization` header:

```
Authorization: Bearer eyJhbGc...token...
```

### Example with cURL
```bash
curl -X GET http://localhost:8080/api/messages \
  -H "Authorization: Bearer eyJhbGc...token..."
```

### Example with JavaScript/Fetch
```javascript
const response = await fetch('http://localhost:8080/api/messages', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});
```

## Protected Routes

### Admin Routes (Require ROLE_ADMIN)
- `GET /api/admin/stats`
- `GET /api/admin/logs`
- `GET /api/admin/users`
- `PUT /api/admin/users/{id}/block`
- `DELETE /api/admin/users/{id}`

### User Routes (Require Authentication)
- `GET /api/messages`
- `POST /api/messages/send`
- `GET /api/users/{id}`
- Any other authenticated endpoint

### Public Routes (No Authentication Required)
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/health`

## Security Features

1. **Token Expiration:** Access tokens expire after 24 hours
2. **Refresh Tokens:** Separate refresh tokens valid for 7 days
3. **HMAC-SHA512:** Tokens are signed with HMAC-SHA512 algorithm
4. **Role-Based Access Control:** JWT includes user role for authorization
5. **Stateless Authentication:** No session required, pure JWT-based

## Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:45",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/auth/login"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:45",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/admin/stats"
}
```

## Implementation Details

### JwtTokenProvider
- **Location:** `messagerie.backend.security.JwtTokenProvider`
- **Responsibilities:**
  - Generate access tokens
  - Generate refresh tokens
  - Validate tokens
  - Extract claims (email, role)

### JwtAuthFilter
- **Location:** `messagerie.backend.security.JwtAuthFilter`
- **Responsibilities:**
  - Intercepts all requests
  - Extracts JWT from Authorization header
  - Validates token
  - Sets authentication in Spring Security context

### GlobalExceptionHandler
- **Location:** `messagerie.backend.config.GlobalExceptionHandler`
- **Responsibilities:**
  - Handles all application exceptions
  - Provides consistent error responses
  - Includes timestamp, status, error message

## Testing JWT Endpoints

### Using Postman

1. **Login:**
   - Method: POST
   - URL: http://localhost:8080/api/auth/login
   - Body (JSON):
     ```json
     {
       "email": "test@example.com",
       "password": "password123"
     }
     ```

2. **Use Token:**
   - Add to Authorization header
   - Type: Bearer Token
   - Token: `{accessToken from login response}`

3. **Refresh Token:**
   - Method: POST
   - URL: http://localhost:8080/api/auth/refresh
   - Body (JSON):
     ```json
     {
       "token": "{refreshToken}"
     }
     ```

## Token Structure

JWT tokens follow the standard structure: `header.payload.signature`

### Header
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

### Payload (Access Token)
```json
{
  "sub": "user@example.com",
  "role": "USER",
  "iat": 1705321445,
  "exp": 1705407845
}
```

### Payload (Refresh Token)
```json
{
  "sub": "user@example.com",
  "iat": 1705321445,
  "exp": 1706008245
}
```

## Next Steps

1. Implement TOTP 2FA verification endpoint
2. Add logout endpoint with token blacklist
3. Implement rate limiting for auth endpoints
4. Add email verification for registration
5. Implement password reset functionality
