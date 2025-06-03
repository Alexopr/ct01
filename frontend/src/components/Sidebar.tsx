import React from 'react';
import { Link as RouterLink, useLocation } from 'react-router-dom';
import { Icon } from "@iconify/react";
import { Avatar, Chip, Divider, Button } from "@nextui-org/react";
import { RoleGuard } from './auth';
import { useAuth } from '../context/AuthContext';

const mainMenu = [
  { icon: "solar:home-bold", label: 'Главная', route: '/' },
  { icon: "solar:widget-bold", label: 'Dashboard', route: '/dashboard' },
  { icon: "solar:buildings-3-bold", label: 'Биржи', route: '/exchanges' },
  { icon: "solar:settings-bold", label: 'Инструменты', route: '/tools' },
  { icon: "solar:bell-bing-bold", label: 'Уведомления', route: '/notifications' },
  { icon: "solar:user-bold", label: 'Профиль', route: '/profile' },
  { icon: "solar:info-circle-bold", label: 'О сервисе', route: '/about' },
];

const adminMenu = [
  { icon: "solar:shield-user-bold", label: 'Админ панель', route: '/admin' },
  { icon: "solar:users-group-rounded-bold", label: 'Пользователи', route: '/admin/users' },
  { icon: "solar:settings-bold", label: 'Настройки', route: '/settings' },
];

const Sidebar: React.FC<{ isOpen?: boolean; onClose?: () => void }> = ({ 
  isOpen = false, 
  onClose 
}) => {
  const { user, isAuthenticated } = useAuth();
  const location = useLocation();

  const isActiveRoute = (route: string) => {
    // Точное совпадение для главной страницы
    if (route === '/' && location.pathname === '/') return true;
    
    // Для всех остальных маршрутов проверяем точное совпадение или вложенные пути
    if (route !== '/' && location.pathname === route) return true;
    
    // Специальная обработка для админ-панели
    if (route === '/admin' && location.pathname === '/admin') return true;
    if (route === '/admin/users' && location.pathname === '/admin/users') return true;
    
    // Для остальных путей проверяем только если это не админ маршруты
    if (route !== '/' && !route.startsWith('/admin') && location.pathname.startsWith(route)) return true;
    
    return false;
  };

  const handleLinkClick = () => {
    if (onClose && window.innerWidth < 768) {
      onClose();
    }
  };

  return (
    <>
      {/* Mobile Overlay */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-black/50 z-40 md:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <div className={`
        w-56 h-screen bg-content1/95 backdrop-blur-lg flex flex-col justify-between border-r border-divider 
        fixed left-0 top-0 z-50 p-4 transform transition-transform duration-300 ease-in-out
        ${isOpen ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0
      `}>
        {/* Close Button (Mobile) */}
        <div className="flex justify-between items-center mb-6 md:hidden">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-gradient-to-r from-primary to-secondary rounded-lg flex items-center justify-center text-white font-black text-lg">
              C
            </div>
            <h1 className="text-lg font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              Menu
            </h1>
          </div>
          <Button
            isIconOnly
            variant="light"
            size="sm"
            onPress={onClose}
            className="text-foreground-600 hover:text-foreground-900"
          >
            <Icon icon="solar:close-circle-bold" width={20} height={20} />
          </Button>
        </div>

        <div>
          {/* Main Menu */}
          <div className="mb-6">
            <h3 className="text-xs font-semibold text-foreground-500 uppercase tracking-wider mb-3 px-2">
              Главное меню
            </h3>
            <nav className="space-y-1">
              {mainMenu.map((item, idx) => (
                <RouterLink 
                  key={idx}
                  to={item.route}
                  onClick={handleLinkClick}
                  className={`
                    flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200
                    ${isActiveRoute(item.route) 
                      ? 'bg-primary/10 text-primary border-l-3 border-primary' 
                      : 'text-foreground-600 hover:bg-content2/70 hover:text-foreground-900'
                    }
                  `}
                >
                  <Icon 
                    icon={item.icon} 
                    width={20} 
                    height={20} 
                    className={isActiveRoute(item.route) ? 'text-primary' : ''} 
                  />
                  <span className={`text-sm font-medium ${isActiveRoute(item.route) ? 'font-semibold' : ''}`}>
                    {item.label}
                  </span>
                </RouterLink>
              ))}
            </nav>
          </div>

          {/* Admin Panel */}
          <RoleGuard requiredRole="ADMIN">
            <div className="mb-6">
              <h3 className="text-xs font-semibold text-foreground-500 uppercase tracking-wider mb-3 px-2">
                Панель администратора
              </h3>
              <nav className="space-y-1">
                {adminMenu.map((item, idx) => (
                  <RouterLink 
                    key={idx}
                    to={item.route}
                    onClick={handleLinkClick}
                    className={`
                      flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200
                      ${isActiveRoute(item.route) 
                        ? 'bg-danger/10 text-danger border-l-3 border-danger' 
                        : 'text-foreground-600 hover:bg-content2/70 hover:text-foreground-900'
                      }
                    `}
                  >
                    <Icon 
                      icon={item.icon} 
                      width={20} 
                      height={20} 
                      className={isActiveRoute(item.route) ? 'text-danger' : ''} 
                    />
                    <span className={`text-sm font-medium ${isActiveRoute(item.route) ? 'font-semibold' : ''}`}>
                      {item.label}
                    </span>
                  </RouterLink>
                ))}
              </nav>
            </div>
          </RoleGuard>

          <Divider className="my-4" />
        </div>

        {/* User Profile */}
        {isAuthenticated() && user && (
          <div className="p-3 border-t border-divider/20 bg-content2/30 rounded-lg">
            <div className="flex items-center gap-3 mb-3">
              <Avatar
                src={user.telegramPhotoUrl}
                name={user.firstName?.[0] || user.username?.[0] || 'U'}
                size="md"
                className="bg-primary text-primary-foreground"
              />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-foreground truncate">
                  {user.firstName || user.username}
                </p>
                <p className="text-xs text-foreground-500 truncate">
                  @{user.username || `user_${user.telegramId}`}
                </p>
              </div>
            </div>
            
            {/* User Roles */}
            <div className="flex flex-wrap gap-1">
              {user.roles.map((role) => (
                <Chip
                  key={role.id}
                  size="sm"
                  variant="flat"
                  color={role.name === 'ADMIN' ? 'danger' : 'primary'}
                  className="text-xs"
                >
                  {role.name}
                </Chip>
              ))}
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default Sidebar; 
