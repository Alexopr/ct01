# Role-Based User Management System API Documentation

## Overview

This document describes the comprehensive role-based user management system implemented in the application. The system provides secure user registration, automatic role assignment, and fine-grained access control based on roles and permissions.

## Table of Contents

1. [Role Hierarchy](#role-hierarchy)
2. [Permissions System](#permissions-system)
3. [API Endpoints](#api-endpoints)
4. [Frontend Integration Guidelines](#frontend-integration-guidelines)
5. [Security Considerations](#security-considerations)
6. [Examples](#examples)
7. [Migration Guide](#migration-guide)

## Role Hierarchy

The system implements a four-tier role hierarchy with automatic permission inheritance:

### Roles

| Role ID | Role Name | Description | Inherits From |
|---------|-----------|-------------|---------------|
| 1 | USER | Base role for all registered users | - |
| 2 | PREMIUM | Paid users with access to premium tools | USER |
| 3 | MODERATOR | Content moderators with user management capabilities | PREMIUM |
| 4 | ADMIN | System administrators with full access | MODERATOR |

### Permission Inheritance

Higher-level roles automatically inherit all permissions from lower-level roles:
- **ADMIN** has all MODERATOR + PREMIUM + USER permissions
- **MODERATOR** has all PREMIUM + USER permissions  
- **PREMIUM** has all USER permissions
- **USER** has base permissions only

## Permissions System

### Available Permissions

| Permission | Description | Default Roles |
|------------|-------------|---------------|
| `USER_READ` | View user information | MODERATOR, ADMIN |
| `USER_WRITE` | Create and update users | MODERATOR, ADMIN |
| `USER_DELETE` | Delete users | ADMIN |
| `COIN_READ` | View cryptocurrency data | USER, PREMIUM, MODERATOR, ADMIN |
| `COIN_WRITE` | Modify cryptocurrency data | MODERATOR, ADMIN |
| `EXCHANGE_READ` | View exchange information | USER, PREMIUM, MODERATOR, ADMIN |
| `EXCHANGE_WRITE` | Modify exchange settings | ADMIN |
| `TOOLS_BASIC` | Access basic tools | USER, PREMIUM, MODERATOR, ADMIN |
| `TOOLS_PREMIUM` | Access premium tools | PREMIUM, MODERATOR, ADMIN |
| `NOTIFICATION_READ` | View notifications | USER, PREMIUM, MODERATOR, ADMIN |
| `NOTIFICATION_WRITE` | Manage notifications | MODERATOR, ADMIN |
| `SYSTEM_READ` | View system settings | ADMIN |
| `SYSTEM_WRITE` | Modify system settings | ADMIN |
| `SYSTEM_ADMIN` | Full system administration | ADMIN |

## API Endpoints

### Authentication Endpoints

#### User Registration
```http
POST /api/auth/register
```

**Description:** Register a new user with automatic USER role assignment.

**Request Body:**
```json
{
  "username": "string",
  "email": "string", 
  "password": "string",
  "firstName": "string",
  "lastName": "string"
}
```

**Response:**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "newuser",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": [
      {
        "id": 1,
        "name": "USER",
        "permissions": [...]
      }
    ],
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "success": true
}
```

**Security:**
- Rate limited to prevent spam registration
- Passwords are automatically hashed
- USER role (ID: 1) is automatically assigned
- Users cannot specify roles during registration

#### Telegram Authentication
```http
POST /api/auth/telegram
```

**Description:** Authenticate user via Telegram and auto-assign USER role.

**Request Body:**
```json
{
  "id": 123456789,
  "first_name": "John",
  "last_name": "Doe", 
  "username": "johndoe",
  "photo_url": "https://...",
  "auth_date": 1640995200,
  "hash": "telegram_hash"
}
```

### User Management Endpoints

#### Get Current User
```http
GET /api/auth/me
```

**Authorization:** Required (any authenticated user)

**Response:**
```json
{
  "id": 1,
  "username": "user",
  "email": "user@example.com",
  "roles": [
    {
      "id": 1,
      "name": "USER",
      "permissions": [
        {
          "id": 1,
          "name": "COIN_READ"
        }
      ]
    }
  ]
}
```

#### List All Users
```http
GET /api/v1/users
```

**Authorization:** Requires `USER_READ` permission or `ADMIN` role

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (default: id)

#### Create User (Admin)
```http
POST /api/v1/users
```

**Authorization:** Requires `USER_WRITE` permission or `ADMIN` role

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string",
  "roles": [1, 2]
}
```

#### Update User
```http
PUT /api/v1/users/{id}
```

**Authorization:** Requires `USER_WRITE` permission, `ADMIN` role, or user updating their own profile

#### Upgrade User to Premium
```http
PUT /api/v1/users/{id}/upgrade-to-premium
```

**Authorization:** Requires `ADMIN` role or `SYSTEM_ADMIN` authority

**Description:** Adds PREMIUM role to user (idempotent operation).

**Response:**
```json
{
  "message": "User successfully upgraded to PREMIUM",
  "user": {
    "id": 1,
    "username": "user",
    "roles": [
      {"id": 1, "name": "USER"},
      {"id": 2, "name": "PREMIUM"}
    ]
  },
  "success": true
}
```

### Role Management Endpoints

#### Assign Role to User
```http
POST /api/v1/users/{userId}/roles/{roleId}
```

**Authorization:** Requires `ROLE_WRITE` permission or `ADMIN` role

#### Remove Role from User
```http
DELETE /api/v1/users/{userId}/roles/{roleId}
```

**Authorization:** Requires `ROLE_WRITE` permission or `ADMIN` role

#### Get All Roles
```http
GET /api/v1/roles
```

**Authorization:** Requires `ROLE_READ` permission or `ADMIN` role

**Response:**
```json
[
  {
    "id": 1,
    "name": "USER",
    "description": "Base role for all users",
    "permissions": [...]
  },
  {
    "id": 2,
    "name": "PREMIUM", 
    "description": "Premium users with advanced features",
    "permissions": [...]
  }
]
```

## Frontend Integration Guidelines

### TypeScript Types

```typescript
// User type with roles as objects
interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: Role[];
  createdAt: string;
}

interface Role {
  id: number;
  name: string;
  description?: string;
  permissions: Permission[];
}

interface Permission {
  id: number;
  name: string;
  description?: string;
}

// For user registration (excludes roles)
interface UserRegistrationData {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

// For admin user management (includes roles)
interface UserFormData {
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  password?: string;
  roles?: number[];
}
```

### Role-Based Components

#### useRole Hook
```typescript
import { useRole } from '../hooks/useRole';

const MyComponent = () => {
  const { 
    hasRole, 
    hasPermission, 
    hasAnyRole, 
    hasAllRoles,
    hasRoleOrHigher,
    isAuthenticated 
  } = useRole();

  if (!hasPermission('TOOLS_PREMIUM')) {
    return <div>Access denied</div>;
  }

  return <PremiumFeature />;
};
```

#### RoleGuard Component
```typescript
import { RoleGuard } from '../components/auth';

// Require specific role
<RoleGuard requiredRole="ADMIN">
  <AdminPanel />
</RoleGuard>

// Require any of multiple roles
<RoleGuard requiredRoles={["ADMIN", "MODERATOR"]} requireAll={false}>
  <ModerationTools />
</RoleGuard>

// Require all specified roles
<RoleGuard requiredRoles={["PREMIUM", "VERIFIED"]} requireAll={true}>
  <ExclusiveFeature />
</RoleGuard>
```

#### ProtectedRoute Component
```typescript
import { ProtectedRoute } from '../components/auth';

<ProtectedRoute requiredRole="ADMIN" redirectTo="/unauthorized">
  <AdminDashboard />
</ProtectedRoute>
```

### Role Display Utilities

```typescript
// Get user-friendly role name
const getRoleDisplayName = (roleName: string): string => {
  const roleNames = {
    'USER': 'Пользователь',
    'PREMIUM': 'Премиум',
    'MODERATOR': 'Модератор', 
    'ADMIN': 'Администратор'
  };
  return roleNames[roleName] || roleName;
};

// Get role color for UI
const getRoleColor = (roleName: string) => {
  const colors = {
    'USER': 'primary',
    'PREMIUM': 'warning',
    'MODERATOR': 'secondary',
    'ADMIN': 'danger'
  };
  return colors[roleName] || 'default';
};
```

## Security Considerations

### Backend Security

1. **Permission-Based Authorization:** All endpoints use `@PreAuthorize` annotations with specific permissions
2. **Role Hierarchy:** Higher roles automatically inherit lower role permissions
3. **Immutable Registration:** Users cannot self-assign roles during registration
4. **Idempotent Operations:** Role assignments are idempotent to prevent duplicates
5. **Audit Logging:** All role changes are logged for security auditing

### Frontend Security

1. **Type Safety:** TypeScript ensures proper role/permission handling
2. **Client-Side Guards:** Components check permissions before rendering
3. **Secure State Management:** User roles stored in secure context
4. **Permission Checks:** UI elements conditionally rendered based on permissions

### Best Practices

1. **Least Privilege:** Grant minimum required permissions
2. **Regular Audits:** Review user roles and permissions regularly
3. **Session Management:** Properly handle authentication tokens
4. **Error Handling:** Don't expose sensitive information in error messages

## Examples

### Frontend Examples

#### Conditional UI Rendering
```typescript
const UserProfile = () => {
  const { user, hasPermission } = useRole();

  return (
    <div>
      <h1>Profile: {user?.firstName}</h1>
      
      {hasPermission('USER_WRITE') && (
        <button>Edit Users</button>
      )}
      
      {hasRole('ADMIN') && (
        <AdminControls />
      )}

      <div>
        Your roles: {user?.roles.map(role => (
          <Chip key={role.id} color={getRoleColor(role.name)}>
            {getRoleDisplayName(role.name)}
          </Chip>
        ))}
      </div>
    </div>
  );
};
```

#### Registration Form
```typescript
const RegisterForm = () => {
  const [formData, setFormData] = useState<UserRegistrationData>({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: ''
    // Note: roles field is intentionally excluded
  });

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    // USER role will be automatically assigned by backend
    await userService.register(formData);
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields - no role selection */}
    </form>
  );
};
```

#### Admin User Management
```typescript
const UserManagement = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);

  const handleRoleToggle = async (userId: number, roleId: number, hasRole: boolean) => {
    if (hasRole) {
      await userService.removeRole(userId, roleId);
    } else {
      await userService.assignRole(userId, roleId);
    }
    // Refresh user list
    loadUsers();
  };

  return (
    <div>
      {users.map(user => (
        <div key={user.id}>
          <span>{user.username}</span>
          
          {/* Display current roles */}
          {user.roles.map(role => (
            <Chip key={role.id}>{role.name}</Chip>
          ))}
          
          {/* Role toggle buttons */}
          {roles.map(role => {
            const hasRole = user.roles.some(ur => ur.id === role.id);
            return (
              <Button
                key={role.id}
                variant={hasRole ? "solid" : "ghost"}
                onClick={() => handleRoleToggle(user.id, role.id, hasRole)}
              >
                {role.name}
              </Button>
            );
          })}
        </div>
      ))}
    </div>
  );
};
```

### Backend Examples

#### Service Layer Role Assignment
```java
@Service
public class UserService {
    
    // Automatic USER role assignment during registration
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        User user = new User();
        // Set user fields...
        
        // Always assign USER role (id: 1) for new registrations
        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"));
        user.setRoles(Set.of(userRole));
        
        User saved = userRepository.save(user);
        return convertToDto(saved);
    }
    
    // Idempotent premium upgrade
    public UserDto upgradeToPremium(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Role premiumRole = roleRepository.findByName("PREMIUM")
            .orElseThrow(() -> new RuntimeException("PREMIUM role not found"));
            
        // Check if user already has PREMIUM role (idempotency)
        boolean hasRole = user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("PREMIUM"));
            
        if (!hasRole) {
            user.getRoles().add(premiumRole);
            user = userRepository.save(user);
        }
        
        return convertToDto(user);
    }
}
```

#### Controller Security Annotations
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserManagementController {
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        // Implementation
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserFormData userData) {
        // Implementation
    }
    
    @PutMapping("/{userId}/upgrade-to-premium")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> upgradeToPremium(@PathVariable Long userId) {
        // Implementation
    }
}
```

## Migration Guide

### Updating Existing Code

1. **Update User Type References:**
   ```typescript
   // Before (roles as strings)
   user.roles.includes('ADMIN')
   
   // After (roles as objects)
   user.roles.some(role => role.name === 'ADMIN')
   ```

2. **Update Registration Forms:**
   ```typescript
   // Remove role selection from registration
   // Use UserRegistrationData instead of UserFormData
   ```

3. **Update Authorization Checks:**
   ```typescript
   // Use permission-based checks when possible
   hasPermission('USER_WRITE') // Preferred
   hasRole('ADMIN') // When role-specific logic needed
   ```

### Database Migration

The system automatically applies database migrations that:
- Create roles and permissions tables
- Establish role-permission relationships  
- Migrate existing user data to new role structure
- Set up proper foreign key constraints

### Testing Migration

1. **Verify Role Assignment:** Check that new registrations get USER role
2. **Test Permission Inheritance:** Confirm higher roles have lower role permissions
3. **Validate UI Changes:** Ensure role-based components render correctly
4. **Check API Security:** Verify endpoint protection works as expected

## Troubleshooting

### Common Issues

1. **"User type conflicts":** Ensure using User from `types/api.ts`, not `types/auth.ts`
2. **"Role assignment failed":** Verify USER role exists in database with ID 1
3. **"Permission denied":** Check user has required permission, not just role
4. **"Registration without roles":** Use UserRegistrationData type for registration

### Debugging Tips

1. Check browser console for authentication errors
2. Verify API responses include proper role structure
3. Use React DevTools to inspect user context state
4. Check backend logs for authorization failures

---

*This documentation covers the complete role-based user management system. For additional questions or clarification, please refer to the codebase or contact the development team.* 