import React from 'react';
import { useRole } from '../../hooks/useRole';

interface RoleGuardProps {
  children: React.ReactNode;
  requiredRole?: string;
  requiredRoles?: string[];
  requireAll?: boolean; // true = все роли, false = любая роль
  fallback?: React.ReactNode;
  requireAuth?: boolean; // требовать авторизацию
}

const RoleGuard: React.FC<RoleGuardProps> = ({
  children,
  requiredRole,
  requiredRoles,
  requireAll = false,
  fallback = null,
  requireAuth = true,
}) => {
  const { isAuthenticated, hasRole, hasAnyRole, hasAllRoles } = useRole();

  // Если требуется авторизация, но пользователь не авторизован
  if (requireAuth && !isAuthenticated) {
    return <>{fallback}</>;
  }

  // Если роли не указаны, показываем контент (если авторизован или авторизация не требуется)
  if (!requiredRole && (!requiredRoles || requiredRoles.length === 0)) {
    return <>{children}</>;
  }

  // Проверяем роли
  let hasAccess = true;

  if (requiredRole) {
    hasAccess = hasRole(requiredRole);
  } else if (requiredRoles && requiredRoles.length > 0) {
    hasAccess = requireAll 
      ? hasAllRoles(requiredRoles)
      : hasAnyRole(requiredRoles);
  }

  return hasAccess ? <>{children}</> : <>{fallback}</>;
};

export default RoleGuard; 