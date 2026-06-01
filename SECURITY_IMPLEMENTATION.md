# Route Security Implementation Summary

## What Was Implemented

### 1. ✅ Role-Based Access Control (RBAC)
- **Admin Routes** (`/api/admin/**`): Protected with `@PreAuthorize("hasRole('ADMIN')")`
- **User Routes** (`/api/users/**`): Protected with `@PreAuthorize("isAuthenticated()")`
- **Message Routes** (`/api/messages/**`): Protected with `@PreAuthorize("isAuthenticated()")`
- All authenticated routes require valid JWT token in `Authorization: Bearer <token>` header

### 2. ✅ User Data Isolation
- **UserController**:
  - Users can only view their own profile
  - Users can only delete their own account
  - Admins can view/delete any user
  - Block/unblock operations restricted to ADMIN role

- **MessageController**:
  - Users can only send messages as themselves
  - Users can only read/delete messages they sent or received
  - Current user extracted from JWT token automatically
  - Ownership verification on all message operations

### 3. ✅ Security Utilities
Created `SecurityUtil.java` with helper methods:
```java
SecurityUtil.getCurrentUserEmail()   // Get authenticated user's email
SecurityUtil.isAdmin()                // Check if user is admin
SecurityUtil.isAuthenticated()        // Check if user is authenticated
```

### 4. ✅ Enhanced SecurityConfig
- Enabled method-level security with `@EnableMethodSecurity(prePostEnabled = true)`
- Configured HTTP request authorization rules
- JWT authentication filter properly integrated
- CORS configuration for frontend integration

## Security Matrix

| Route | Method | Public | USER | ADMIN | Notes |
|-------|--------|--------|------|-------|-------|
| /api/auth/** | ALL | ✅ | ✅ | ✅ | Register, Login, Refresh |
| /api/health | GET | ✅ | ✅ | ✅ | Health check |
| /api/admin/** | ALL | ❌ | ❌ | ✅ | Admin only |
| /api/users/{id} | GET | ❌ | ✅* | ✅ | *Own profile or admin |
| /api/users/{id} | DELETE | ❌ | ✅* | ✅ | *Own account or admin |
| /api/users/{id}/block | PUT | ❌ | ❌ | ✅ | Admin only |
| /api/messages/send | POST | ❌ | ✅ | ✅ | Authenticated only |
| /api/messages/unread | GET | ❌ | ✅ | ✅ | Own messages |
| /api/messages/{id}/read | PUT | ❌ | ✅ | ✅ | Ownership check |
| /api/messages/{id} | DELETE | ❌ | ✅ | ✅ | Ownership check |
| /api/messages/conversation/{id} | GET | ❌ | ✅ | ✅ | Ownership check |

## Code Changes Made

1. **SecurityConfig.java**
   - Added `@EnableMethodSecurity(prePostEnabled = true)`
   - Added more specific route patterns for better security

2. **AdminController.java**
   - Added `@PreAuthorize("hasRole('ADMIN')")` at class level

3. **UserController.java**
   - Added `@PreAuthorize("isAuthenticated()")` at class level
   - Added ownership checks for individual user access
   - Restricted block/unblock to admins only
   - Added AuthService injection for user lookup

4. **MessageController.java**
   - Added `@PreAuthorize("isAuthenticated()")` at class level
   - Removed manual `X-User-Id` header extraction
   - Uses JWT token to get current user email
   - Added ownership checks on all message operations
   - Added MessageService.getMessageById() method

5. **AuthService.java**
   - Added `findByEmail(String email)` public method

6. **MessageService.java**
   - Added `getMessageById(Long messageId)` method

7. **New File: SecurityUtil.java**
   - Helper utility for security checks
   - Extracts current user info from SecurityContext

## How It Works

### Authentication Flow
1. User logs in with email/password
2. Backend validates credentials and returns JWT token
3. Frontend stores JWT in localStorage
4. Frontend sends JWT in every request: `Authorization: Bearer <token>`
5. Backend validates JWT and extracts user identity
6. Spring Security enforces role-based access

### Authorization Flow
1. Request arrives with JWT token
2. JwtAuthFilter extracts and validates token
3. User email and role extracted from JWT claims
4. Spring Security context populated with authorities
5. @PreAuthorize annotations enforce role requirements
6. Controller methods check data ownership
7. Response returned or 403 Forbidden

### Example: Sending a Message
```
1. User sends: POST /api/messages/send (with JWT)
2. JwtAuthFilter extracts email from JWT
3. AuthService converts email to user ID
4. Message created with senderId = current user ID
5. Recipient validates if user can send to them
6. Message saved to database
```

### Example: Accessing User Profile
```
1. User sends: GET /api/users/5 (with JWT)
2. JwtAuthFilter extracts user ID from JWT (e.g., user ID 3)
3. Controller checks: Is user 3 admin OR is user 3 trying to access user 3?
4. If YES → Return profile
5. If NO → Return 403 Forbidden
```

## Testing the Implementation

### 1. Start the backend
```bash
cd c:\Java\messagerie\backend
mvn spring-boot:run
```

### 2. Register/Login as regular user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"John",
    "lastName":"Doe",
    "email":"user@example.com",
    "password":"Password123!",
    "phone":"+1234567890",
    "twoFactorEnabled":false
  }'
```

### 3. Try to access admin routes (should fail)
```bash
curl -X GET http://localhost:8080/api/admin/stats \
  -H "Authorization: Bearer <YOUR_TOKEN>"
# Response: 403 Forbidden
```

### 4. Access own user profile (should work)
```bash
curl -X GET "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
# Response: 200 OK with user data
```

### 5. Try to access another user's profile (should fail)
```bash
curl -X GET "http://localhost:8080/api/users/2" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
# Response: 403 Forbidden
```

## Next Steps

1. ✅ Backend routes are now secured
2. Next: Update frontend API client to use JWT authentication
3. Add error handling for 403 responses in frontend
4. Add logout functionality to clear JWT token
5. Consider adding refresh token rotation
6. Monitor security logs for unauthorized access attempts

## Files Modified
- SecurityConfig.java
- AdminController.java
- UserController.java
- MessageController.java
- AuthService.java
- MessageService.java

## Files Created
- SecurityUtil.java
- SECURITY.md (this documentation)
