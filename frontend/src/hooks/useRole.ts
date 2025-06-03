import { useAuth } from '../context/AuthContext';

export const useRole = () => {
  const { user, isAuthenticated } = useAuth();

  const hasRole = (role: string): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return user.roles.some(r => r.name === role);
  };

  const hasPermission = (permission: string): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return user.roles.some(role => 
      role.permissions?.some(perm => perm.name === permission)
    );
  };

  const hasAnyRole = (roles: string[]): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return roles.some(role => user.roles.some(r => r.name === role));
  };

  const hasAllRoles = (roles: string[]): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return roles.every(role => user.roles.some(r => r.name === role));
  };

  const hasAnyPermission = (permissions: string[]): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return permissions.some(permission => 
      user.roles.some(role => 
        role.permissions?.some(perm => perm.name === permission)
      )
    );
  };

  const hasAllPermissions = (permissions: string[]): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return permissions.every(permission => 
      user.roles.some(role => 
        role.permissions?.some(perm => perm.name === permission)
      )
    );
  };

  // Иерархия ролей: ADMIN > MODERATOR > PREMIUM > USER
  const hasRoleOrHigher = (requiredRole: string): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }

    const roleHierarchy = {
      'USER': 1,
      'PREMIUM': 2,
      'MODERATOR': 3,
      'ADMIN': 4
    };

    const userHighestRole = Math.max(
      ...user.roles.map(role => roleHierarchy[role.name as keyof typeof roleHierarchy] || 0)
    );

    const requiredRoleLevel = roleHierarchy[requiredRole as keyof typeof roleHierarchy] || 0;

    return userHighestRole >= requiredRoleLevel;
  };

  return {
    user,
    isAuthenticated: isAuthenticated(),
    isAdmin: hasRole('ADMIN'),
    isUser: hasRole('USER'),
    isPremium: hasRole('PREMIUM'),
    isModerator: hasRole('MODERATOR'),
    hasRole,
    hasPermission,
    hasAnyRole,
    hasAllRoles,
    hasAnyPermission,
    hasAllPermissions,
    hasRoleOrHigher,
  };
}; 
