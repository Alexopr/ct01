import React, { useState, useEffect } from 'react';
import { Card, Button, Alert, Chip } from '../components/ui';
import { Icon } from '@iconify/react';
import { ProtectedRoute } from '../components/auth';
import api from '../utils/api';
import type { User } from '../types/api';

interface AdminStats {
  totalUsers: number;
  telegramUsers: number;
  adminUsers: number;
}

const AdminPanel: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [usersResponse, statsResponse] = await Promise.all([
        api.get('/admin/users'),
        api.get('/admin/stats'),
      ]);

      setUsers(usersResponse.data);
      setStats(statsResponse.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка загрузки данных');
    } finally {
      setLoading(false);
    }
  };

  const getRoleColor = (role: string): 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'danger' => {
    switch (role) {
      case 'ADMIN':
        return 'danger';
      case 'PREMIUM':
        return 'warning';
      case 'MODERATOR':
        return 'secondary';
      case 'USER':
        return 'primary';
      default:
        return 'default';
    }
  };

  const getRoleDisplayName = (roleName: string): string => {
    switch (roleName) {
      case 'ADMIN':
        return 'Администратор';
      case 'PREMIUM':
        return 'Премиум';
      case 'MODERATOR':
        return 'Модератор';
      case 'USER':
        return 'Пользователь';
      default:
        return roleName;
    }
  };

  const statsCards = [
    {
      title: 'Всего пользователей',
      value: stats?.totalUsers || 0,
      icon: 'solar:users-group-two-rounded-bold',
      color: 'primary',
      bgGradient: 'from-primary/20 to-primary/30'
    },
    {
      title: 'Telegram пользователи',
      value: stats?.telegramUsers || 0,
      icon: 'solar:chat-round-bold',
      color: 'secondary',
      bgGradient: 'from-secondary/20 to-secondary/30'
    },
    {
      title: 'Администраторы',
      value: stats?.adminUsers || 0,
      icon: 'solar:shield-user-bold',
      color: 'danger',
      bgGradient: 'from-danger/20 to-danger/30'
    }
  ];

  return (
    <ProtectedRoute requiredRole="ADMIN">
      <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6">
        <div className="max-w-7xl mx-auto space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
          
          {/* Header */}
          <div className="flex items-center gap-4">
            <div className="inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r from-danger to-warning rounded-xl">
              <Icon icon="solar:shield-user-bold" className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-danger to-warning bg-clip-text text-transparent">
                Панель администратора
              </h1>
              <p className="text-foreground-600">Управление пользователями и системой</p>
            </div>
          </div>

          {error && (
            <Alert
              type="error"
              title="Ошибка"
              description={error}
              className="animate-in fade-in-0 slide-in-from-top-4 duration-500"
            />
          )}

          {loading ? (
            <div className="space-y-6">
              {/* Loading Stats */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {[1, 2, 3].map((i) => (
                  <Card key={i}  className="p-6 animate-pulse">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 bg-foreground-200 rounded-xl"></div>
                      <div className="space-y-2 flex-1">
                        <div className="h-4 bg-foreground-200 rounded w-3/4"></div>
                        <div className="h-8 bg-foreground-200 rounded w-1/2"></div>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
              
              {/* Loading Table */}
              <Card  className="p-6">
                <div className="space-y-4">
                  <div className="h-6 bg-foreground-200 rounded w-1/4"></div>
                  {[1, 2, 3, 4, 5].map((i) => (
                    <div key={i} className="h-12 bg-foreground-200 rounded"></div>
                  ))}
                </div>
              </Card>
            </div>
          ) : (
            <>
              {/* Statistics Cards */}
              {stats && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  {statsCards.map((stat, index) => (
                    <Card
                      key={stat.title} className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl transition-all duration-500 hover:shadow-2xl animate-in fade-in-0 slide-in-from-bottom-4"
                      style={{ animationDelay: `${index * 150}ms` }}
                    >
                      <div className="p-6">
                        <div className="flex items-center gap-4">
                          <div className={`inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r ${stat.bgGradient} rounded-xl`}>
                            <Icon icon={stat.icon} className={`w-6 h-6 text-${stat.color}`} />
                          </div>
                          <div className="flex-1">
                            <p className="text-sm text-foreground-600 font-medium">{stat.title}</p>
                            <p className="text-2xl font-bold text-foreground">{stat.value}</p>
                          </div>
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>
              )}

              {/* Users Table */}
              <Card
                
                className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-bottom-4 duration-700"
                style={{ animationDelay: '450ms' }}
              >
                <div className="p-6">
                  <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center gap-3">
                      <Icon icon="solar:users-group-rounded-bold" className="w-6 h-6 text-primary" />
                      <h2 className="text-xl font-semibold text-foreground">Пользователи системы</h2>
                    </div>
                    <Button
                      color="primary"
                      size="sm"
                      startContent={<Icon icon="solar:refresh-bold" className="w-4 h-4" />}
                      onClick={fetchData}
                      disabled={loading}
                    >
                      Обновить
                    </Button>
                  </div>

                  {/* Table Header */}
                  <div className="hidden md:grid grid-cols-6 gap-4 p-4 bg-foreground-50 rounded-lg mb-4 text-sm font-semibold text-foreground-700">
                    <div>ID</div>
                    <div>Пользователь</div>
                    <div>Имя</div>
                    <div>Telegram ID</div>
                    <div>Роли</div>
                    <div>Дата создания</div>
                  </div>

                  {/* Table Body */}
                  <div className="space-y-3">
                    {users.map((user, index) => (
                      <div
                        key={user.id}
                        className="grid grid-cols-1 md:grid-cols-6 gap-4 p-4 bg-background/50 rounded-lg border border-divider/20 hover:bg-background/70 transition-colors duration-200 animate-in fade-in-0 slide-in-from-left-4"
                        style={{ animationDelay: `${index * 50}ms` }}
                      >
                        {/* Mobile Layout */}
                        <div className="md:hidden space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="font-semibold text-foreground">#{user.id}</span>
                            <div className="flex gap-1">
                              {user.roles.map((role) => (
                                <Chip
                                  key={role.id}
                                  color={getRoleColor(role.name)}
                                  size="sm"
                                >
                                  {getRoleDisplayName(role.name)}
                                </Chip>
                              ))}
                            </div>
                          </div>
                          <div className="text-foreground">{user.username || '-'}</div>
                          <div className="text-sm text-foreground-600">
                            {user.firstName && user.lastName 
                              ? `${user.firstName} ${user.lastName}`
                              : user.firstName || '-'
                            }
                          </div>
                          <div className="text-sm text-foreground-600">
                            Telegram: {user.telegramId || '-'}
                          </div>
                          <div className="text-xs text-foreground-500">
                            {user.createdAt 
                              ? new Date(user.createdAt).toLocaleDateString('ru-RU')
                              : '-'
                            }
                          </div>
                        </div>

                        {/* Desktop Layout */}
                        <div className="hidden md:block text-sm font-medium text-foreground">
                          #{user.id}
                        </div>
                        <div className="hidden md:block text-sm text-foreground">
                          {user.username || '-'}
                        </div>
                        <div className="hidden md:block text-sm text-foreground">
                          {user.firstName && user.lastName 
                            ? `${user.firstName} ${user.lastName}`
                            : user.firstName || '-'
                          }
                        </div>
                        <div className="hidden md:block text-sm text-foreground-600">
                          {user.telegramId || '-'}
                        </div>
                        <div className="hidden md:flex gap-1">
                          {user.roles.map((role) => (
                            <Chip
                              key={role.id}
                              color={getRoleColor(role.name)}
                              size="sm"
                            >
                              {getRoleDisplayName(role.name)}
                            </Chip>
                          ))}
                        </div>
                        <div className="hidden md:block text-sm text-foreground-600">
                          {user.createdAt 
                            ? new Date(user.createdAt).toLocaleDateString('ru-RU')
                            : '-'
                          }
                        </div>
                      </div>
                    ))}
                  </div>

                  {users.length === 0 && (
                    <div className="text-center py-12">
                      <Icon icon="solar:users-group-rounded-bold" className="w-16 h-16 text-foreground-400 mx-auto mb-4" />
                      <h3 className="text-lg font-semibold text-foreground mb-2">
                        Пользователи не найдены
                      </h3>
                      <p className="text-foreground-600">
                        В системе пока нет зарегистрированных пользователей
                      </p>
                    </div>
                  )}
                </div>
              </Card>
            </>
          )}
        </div>
      </div>
    </ProtectedRoute>
  );
};

export default AdminPanel;



