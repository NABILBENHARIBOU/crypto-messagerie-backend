# Backend Security Implementation

## Overview
This document describes the security measures implemented in the Messagerie Backend to ensure that routes are properly protected based on user roles.

## Security Architecture

### 1. JWT Authentication Filter
- **Location**: `security/JwtAuthFilter.java`
- **Purpose**: Validates JWT tokens from Authorization header
- **Process**:
  1. Extracts JWT from `Authorization: Bearer <token>` header
  2. Validates token signature and expiration
  3. Extracts email and role from token claims
  4. Sets Spring Security authentication context with authorities

### 2. Role-Based Access Control (RBAC)
- **Enum**: User has `UserRole` enum with values: `ADMIN`, `USER`, `MODERATOR`
- **Token Claims**: Role is stored in JWT token claims
- **Authority Format**: `ROLE_ADMIN`, `ROLE_USER`, `ROLE_MODERATOR` (Spring automatically prefixes with `ROLE_`)

### 3. Security Configuration
**Location**: `config/SecurityConfig.java`

#### Method Security (PreAuthorize)
```
@EnableMethodSecurity(prePostEnabled = true)
```
This enables `@PreAuthorize` annotations on controller methods.

#### Route Protection
```
/api/auth/**        → Public (permitAll)
/api/health         → Public (permitAll)
/api/admin/**       → ADMIN role required
/api/messages/**    → Authenticated users only
/api/users/**       → Authenticated users only
/api/blockchain/**  → Authenticated users only
/api/stegano/**     → Authenticated users only
```

## Controller Security

### AdminController
- **Protection**: `@PreAuthorize("hasRole('ADMIN')")` on class level
- **Routes**:
  - `GET /api/admin/stats` - View platform statistics
  - `GET /api/admin/users` - List all users
  - `PUT /api/admin/users/{id}/block` - Block user
  - `PUT /api/admin/users/{id}/unblock` - Unblock user
  - `DELETE /api/admin/users/{id}` - Delete user
  - `GET /api/admin/logs` - View security logs

### UserController
- **Protection**: `@PreAuthorize("isAuthenticated()")` on class level
- **Rules**:
  - Users can only view their own profile
  - Users can only delete their own account
  - Users cannot block/unblock other users (admin-only)
  - Admins can perform all operations
- **Routes**:
  - `GET /api/users/{id}` - View user profile (own or admin)
  - `GET /api/users/email/{email}` - Get user by email (own or admin)
  - `PUT /api/users/{id}/block` - Block user (admin-only)
  - `PUT /api/users/{id}/unblock` - Unblock user (admin-only)
  - `DELETE /api/users/{id}` - Delete user (own or admin)

### MessageController
- **Protection**: `@PreAuthorize("isAuthenticated()")` on class level
- **Rules**:
  - Users can only send messages as themselves
  - Users can only view messages they sent or received
  - Users can only mark their own messages as read
  - Users can only delete their own messages
- **Routes**:
  - `POST /api/messages/send` - Send message (uses authenticated user)
  - `GET /api/messages/unread` - Get unread messages (for authenticated user)
  - `PUT /api/messages/{id}/read` - Mark message as read (ownership check)
  - `DELETE /api/messages/{id}` - Delete message (ownership check)
  - `GET /api/messages/conversation/{userId}` - Get conversation (between authenticated user and target)

## Utility Functions

### SecurityUtil
**Location**: `util/SecurityUtil.java`

```java
// Get current authenticated user's email
String email = SecurityUtil.getCurrentUserEmail();

// Check if current user is admin
if (SecurityUtil.isAdmin()) { ... }

// Check if user is authenticated
if (SecurityUtil.isAuthenticated()) { ... }
```

## Authorization Examples

### Example 1: User trying to view another user's profile
```
Request: GET /api/users/5 (user 5 is not the authenticated user)
Response: 403 Forbidden - "You don't have permission to access this user"
```

### Example 2: User trying to access admin routes
```
Request: GET /api/admin/stats (user has role USER)
Response: 403 Forbidden - Access denied
```

### Example 3: User accessing their own messages
```
Request: GET /api/messages/conversation/3
(Authenticated user = 2, target user = 3)
Response: 200 OK - Conversation retrieved
```

### Example 4: User trying to delete another user's message
```
Request: DELETE /api/messages/100 (message sent by user 5, authenticated user is 2)
Response: 403 Forbidden - "You don't have permission to delete this message"
```

## Authentication Flow

1. **User Registration**
   ```
   POST /api/auth/register
   → Creates user with role USER
   → Returns JWT access and refresh tokens
   ```

2. **User Login**
   ```
   POST /api/auth/login
   → Validates credentials
   → Generates JWT with email and role in claims
   → Returns access token, refresh token, user data
   ```

3. **Authenticated Request**
   ```
   GET /api/users/5
   Header: Authorization: Bearer <JWT_TOKEN>
   → JwtAuthFilter validates token
   → SecurityContext is populated with user details
   → Controller checks authorization
   → Response returned if authorized
   ```

4. **Token Refresh**
   ```
   POST /api/auth/refresh
   Header: Authorization: Bearer <REFRESH_TOKEN>
   → Generates new access token
   → Returns new JWT pair
   ```

## Security Best Practices

1. **Password Security**: Passwords are hashed with BCrypt (10 rounds)
2. **Token Expiration**: Access tokens expire in 24 hours, refresh tokens in 7 days
3. **HTTPS**: Production should use HTTPS only (useSSL=true)
4. **CORS**: Only whitelisted origins allowed (http://localhost:5173, http://localhost:3000)
5. **Stateless Sessions**: SessionCreationPolicy.STATELESS - no server-side sessions
6. **CSRF Protection**: Disabled for stateless API (appropriate for REST APIs)

## Testing Security

### Test Admin Access
```bash
# Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password"}'

# Use returned token for admin routes
curl -X GET http://localhost:8080/api/admin/stats \
  -H "Authorization: Bearer <TOKEN>"
```

### Test User Restrictions
```bash
# Login as user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Try to access admin route (should fail with 403)
curl -X GET http://localhost:8080/api/admin/stats \
  -H "Authorization: Bearer <TOKEN>"
```

## Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "User not authenticated"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this user"
}
```

## Future Enhancements

1. **Rate Limiting**: Add request rate limiting per user
2. **Audit Logging**: Enhanced logging of all auth/admin operations
3. **Two-Factor Authentication**: Implement TOTP for 2FA
4. **OAuth2**: Add social login providers (Google, GitHub, etc.)
5. **Encryption at Rest**: Encrypt sensitive data in database
6. **IP Whitelisting**: Restrict admin routes to specific IPs
7. **Session Management**: Add ability to revoke tokens/sessions
