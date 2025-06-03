# Project Documentation

This directory contains comprehensive documentation for the role-based user management system and other project components.

## 📚 Documentation Index

### Role Management System
- **[Role Management API](./ROLE_MANAGEMENT_API.md)** - Complete API documentation with examples
- **[Quick Start Guide](./ROLE_MANAGEMENT_QUICKSTART.md)** - Essential patterns for developers

### Other Documentation
- Add other documentation files here as the project grows

## 🚀 Quick Start

New to the project? Start here:

1. **Read** the [Quick Start Guide](./ROLE_MANAGEMENT_QUICKSTART.md) for essential patterns
2. **Reference** the [Full API Documentation](./ROLE_MANAGEMENT_API.md) for detailed implementation
3. **Test** your implementation using the provided examples

## 🔑 Key Concepts

### Role Hierarchy
```
ADMIN (4) → Full system access
    ↓
MODERATOR (3) → User management + premium tools
    ↓  
PREMIUM (2) → Basic tools + premium features
    ↓
USER (1) → Basic access (auto-assigned)
```

### Permission Inheritance
Higher roles automatically inherit all permissions from lower roles.

### Security Model
- **Frontend:** Role-based UI rendering with TypeScript safety
- **Backend:** Permission-based endpoint protection with Spring Security
- **Registration:** Automatic USER role assignment, no self-service role selection

## 🛠️ Development Guidelines

### When to Use Each Type

| Type | Use Case | Has Roles? |
|------|----------|------------|
| `UserRegistrationData` | User registration | ❌ No |
| `UserFormData` | Admin user creation | ✅ Yes |
| `User` | Display user info | ✅ Yes (as objects) |

### Security Best Practices

1. **Use permissions over roles** when possible
2. **Validate on backend** - never trust frontend-only checks
3. **Follow least privilege** - grant minimum required permissions
4. **Audit regularly** - review user roles and permissions

### Code Quality

- Use TypeScript types strictly
- Implement proper error handling
- Add logging for security events
- Write tests for role-based logic

## 📖 API Reference

### Quick Endpoint Reference

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/register` | User registration | No |
| `POST` | `/api/auth/telegram` | Telegram auth | No |
| `GET` | `/api/auth/me` | Current user | Yes |
| `GET` | `/api/v1/users` | List users | USER_READ |
| `PUT` | `/api/v1/users/{id}/upgrade-to-premium` | Upgrade to premium | ADMIN |
| `GET` | `/api/v1/roles` | List roles | ROLE_READ |

## 🧪 Testing

### Test Categories

1. **Unit Tests** - Role assignment, permission checks
2. **Integration Tests** - API endpoint security
3. **UI Tests** - Role-based component rendering
4. **Security Tests** - Authorization bypass attempts

### Testing Checklist

- [ ] Registration assigns USER role automatically
- [ ] Admin can manage user roles
- [ ] Premium upgrade works correctly
- [ ] UI renders based on permissions
- [ ] Protected routes block unauthorized access

## 🐛 Troubleshooting

### Common Issues

| Error | Solution |
|-------|----------|
| Type conflicts between User interfaces | Use User from `types/api.ts` |
| Role assignment failures | Verify USER role exists with ID 1 |
| Permission denied errors | Check permission vs role requirements |
| Registration without roles | Use UserRegistrationData type |

### Debug Steps

1. Check browser console for auth errors
2. Verify API responses include role structure
3. Inspect user context state in React DevTools
4. Review backend logs for authorization failures

## 🔄 Migration Guide

### From String Roles to Object Roles

```typescript
// Before
user.roles.includes('ADMIN')

// After  
user.roles.some(role => role.name === 'ADMIN')
// Or: hasRole('ADMIN')
```

### Registration Form Updates

1. Remove role selection components
2. Change `UserFormData` → `UserRegistrationData`
3. Remove roles from form state
4. Update API calls to registration endpoint

## 📞 Support

For questions about the role management system:

1. **Check** this documentation first
2. **Search** existing issues in the project repository
3. **Review** code examples in the documentation
4. **Contact** the development team if needed

---

## 📋 Documentation Maintenance

This documentation should be updated when:

- New roles or permissions are added
- API endpoints change
- Security model is modified  
- New frontend patterns are established

**Last Updated:** December 2024
**Version:** 1.0.0 