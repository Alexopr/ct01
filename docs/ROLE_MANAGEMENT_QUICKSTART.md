# Role Management System - Quick Start Guide

## For New Developers

### Key Changes Summary

1. **Registration:** Users get USER role automatically, cannot self-assign roles
2. **User Type:** `roles` field is now `Role[]` objects instead of `string[]`
3. **Permission System:** Use permissions instead of role checks when possible
4. **Admin Panel:** Enhanced with role management capabilities

### Essential Code Patterns

#### Check User Permissions
```typescript
// ✅ Preferred: Permission-based
const { hasPermission } = useRole();
if (hasPermission('USER_WRITE')) {
  // Show admin features
}

// ✅ When role-specific logic needed
const { hasRole } = useRole();
if (hasRole('ADMIN')) {
  // Admin-only features
}
```

#### Role-Based UI
```typescript
import { RoleGuard } from '../components/auth';

// Simple role check
<RoleGuard requiredRole="ADMIN">
  <AdminButton />
</RoleGuard>

// Multiple roles (any)
<RoleGuard requiredRoles={["ADMIN", "MODERATOR"]} requireAll={false}>
  <ModeratorTools />
</RoleGuard>
```

#### User Registration (No Role Selection)
```typescript
// ✅ Correct: UserRegistrationData (no roles)
const registerData: UserRegistrationData = {
  username: 'newuser',
  email: 'user@example.com',
  password: 'password',
  firstName: 'John',
  lastName: 'Doe'
  // ❌ roles: [1] // Not allowed
};

await userService.register(registerData);
```

#### Admin User Creation (With Roles)
```typescript
// ✅ Correct: UserFormData (includes roles for admin)
const userData: UserFormData = {
  username: 'admin',
  email: 'admin@example.com',
  password: 'password',
  roles: [1, 4] // USER + ADMIN
};

await userService.createUser(userData);
```

#### Display User Roles
```typescript
// ✅ Correct: Access role.name
{user.roles.map(role => (
  <Chip key={role.id} color={getRoleColor(role.name)}>
    {getRoleDisplayName(role.name)}
  </Chip>
))}

// ❌ Wrong: roles are not strings anymore
{user.roles.map(role => <Chip>{role}</Chip>)}
```

### Backend Security Patterns

#### Endpoint Protection
```java
// ✅ Permission-based (preferred)
@PreAuthorize("hasAuthority('USER_WRITE')")

// ✅ Role-based when needed
@PreAuthorize("hasRole('ADMIN')")

// ✅ Combined
@PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
```

#### Service Layer
```java
// ✅ Auto-assign USER role in registration
Role userRole = roleRepository.findByName("USER")
    .orElseThrow(() -> new RuntimeException("USER role not found"));
user.setRoles(Set.of(userRole));

// ✅ Check permissions in service
if (!user.hasPermission("USER_WRITE")) {
    throw new AccessDeniedException("Insufficient permissions");
}
```

## Common Migration Tasks

### 1. Update Role Checks
```typescript
// Before
if (user.roles.includes('ADMIN')) { }

// After  
if (user.roles.some(role => role.name === 'ADMIN')) { }
// Or better: if (hasRole('ADMIN')) { }
```

### 2. Fix Registration Forms
```typescript
// Remove role selection components
// Change UserFormData → UserRegistrationData
// Remove roles from form state
```

### 3. Update User Display
```typescript
// Before
<span>{user.roles.join(', ')}</span>

// After
<span>{user.roles.map(r => r.name).join(', ')}</span>
```

### 4. Add Permission Checks
```typescript
// Instead of role checks, use permissions
hasPermission('TOOLS_PREMIUM') // vs hasRole('PREMIUM')
```

## Testing Checklist

- [ ] New user registration assigns USER role only
- [ ] Admin can create users with custom roles
- [ ] Premium upgrade endpoint works (admin/payment only)
- [ ] Role-based UI components render correctly
- [ ] Permission inheritance works (ADMIN has USER permissions)
- [ ] Protected routes block unauthorized access
- [ ] Telegram auth assigns USER role automatically

## Troubleshooting

### "Type 'string' is not assignable to type 'Role'"
- Check you're using User from `types/api.ts` not `types/auth.ts`
- Update role access from `role` to `role.name`

### "Cannot read property 'name' of undefined"
- Ensure roles array is initialized
- Check API response includes role objects with name field

### "Access denied" errors
- Verify user has required permission, not just role
- Check @PreAuthorize annotation matches permission name
- Ensure role exists in database with correct permissions

## Quick Reference

### Role Hierarchy
- **USER** (id: 1) → Basic permissions
- **PREMIUM** (id: 2) → USER + premium tools  
- **MODERATOR** (id: 3) → PREMIUM + user management
- **ADMIN** (id: 4) → MODERATOR + full system access

### Key Endpoints
- `POST /api/auth/register` - User registration (auto USER role)
- `PUT /api/v1/users/{id}/upgrade-to-premium` - Premium upgrade
- `POST /api/v1/users/{userId}/roles/{roleId}` - Assign role
- `GET /api/v1/roles` - List all roles

### TypeScript Types
- `UserRegistrationData` - Registration (no roles)
- `UserFormData` - Admin user creation (with roles)  
- `User` - User object with `roles: Role[]`
- `Role` - Role object with `permissions: Permission[]`

---

*For complete documentation, see [ROLE_MANAGEMENT_API.md](./ROLE_MANAGEMENT_API.md)* 