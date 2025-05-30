import { useAuth } from '../context/AuthContext';

export const useRole = () => {
  const { user, isAuthenticated } = useAuth();

  const hasRole = (role: string): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return user.roles.includes(role);
  };

  const hasAnyRole = (roles: string[]): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return roles.some(role => user.roles.includes(role));
  };

  const hasAllRoles = (roles: string[]): boolean => {
    if (!isAuthenticated() || !user?.roles) {
      return false;
    }
    return roles.every(role => user.roles.includes(role));
  };

  return {
    user,
    isAuthenticated: isAuthenticated(),
    isAdmin: hasRole('ADMIN'),
    isUser: hasRole('USER'),
    hasRole,
    hasAnyRole,
    hasAllRoles,
  };
}; 