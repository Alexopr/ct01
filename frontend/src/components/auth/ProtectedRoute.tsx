import React from 'react';
import { Navigate } from 'react-router-dom';
import { useRole } from '../../hooks/useRole';
import { Box, Typography, Alert } from '@mui/material';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: string;
  requiredRoles?: string[];
  requireAll?: boolean; // true = все роли, false = любая роль
  fallback?: React.ReactNode;
  redirectTo?: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole,
  requiredRoles,
  requireAll = false,
  fallback,
  redirectTo = '/dashboard',
}) => {
  const { isAuthenticated, hasRole, hasAnyRole, hasAllRoles } = useRole();

  // Если не авторизован, перенаправляем на главную
  if (!isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
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

  // Если нет доступа
  if (!hasAccess) {
    if (fallback) {
      return <>{fallback}</>;
    }

    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          <Typography variant="h6" gutterBottom>
            Доступ запрещен
          </Typography>
          <Typography variant="body2">
            У вас недостаточно прав для просмотра этой страницы.
          </Typography>
        </Alert>
      </Box>
    );
  }

  return <>{children}</>;
};

export default ProtectedRoute; 