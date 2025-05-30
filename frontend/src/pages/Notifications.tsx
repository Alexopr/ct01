import React, { useState } from "react";
import { Card, Button, Input, Chip } from "../components/ui";
import { Icon } from '@iconify/react';

type NotificationType = 'info' | 'success' | 'warning' | 'error';

interface Notification {
  id: number;
  title: string;
  message: string;
  type: NotificationType;
  timestamp: Date;
  isRead: boolean;
  category: string;
}

const mockNotifications: Notification[] = [
  {
    id: 1,
    title: "Добро пожаловать!",
    message: "Добро пожаловать в Crypto Dashboard! Изучите все возможности платформы.",
    type: "info",
    timestamp: new Date(Date.now() - 2 * 60 * 1000),
    isRead: false,
    category: "system"
  },
  {
    id: 2,
    title: "Пароль изменен",
    message: "Ваш пароль был успешно изменён.",
    type: "success",
    timestamp: new Date(Date.now() - 1 * 60 * 60 * 1000),
    isRead: true,
    category: "security"
  },
  {
    id: 3,
    title: "Подозрительная активность",
    message: "Обнаружена подозрительная активность в вашем аккаунте.",
    type: "warning",
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
    isRead: false,
    category: "security"
  },
];

const Notifications: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>(mockNotifications);
  const [searchTerm, setSearchTerm] = useState('');

  const getTypeConfig = (type: NotificationType) => {
    switch (type) {
      case 'success':
        return { color: 'success' as const, icon: 'solar:check-circle-bold' };
      case 'warning':
        return { color: 'warning' as const, icon: 'solar:danger-triangle-bold' };
      case 'error':
        return { color: 'danger' as const, icon: 'solar:close-circle-bold' };
      default:
        return { color: 'primary' as const, icon: 'solar:info-circle-bold' };
    }
  };

  const formatTimeAgo = (timestamp: Date) => {
    const diffInMs = new Date().getTime() - timestamp.getTime();
    const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));

    if (diffInMinutes < 1) return 'только что';
    if (diffInMinutes < 60) return `${diffInMinutes} мин назад`;
    if (diffInHours < 24) return `${diffInHours} ч назад`;
    return timestamp.toLocaleDateString('ru-RU');
  };

  const handleMarkAsRead = (id: number) => {
    setNotifications(prev => 
      prev.map(notification => 
        notification.id === id ? { ...notification, isRead: true } : notification
      )
    );
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;

  const filteredNotifications = notifications.filter(notification =>
    notification.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    notification.message.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6">
      <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r from-primary to-secondary rounded-xl relative">
              <Icon icon="solar:bell-bing-bold" className="w-6 h-6 text-white" />
              {unreadCount > 0 && (
                <div className="absolute -top-1 -right-1 w-5 h-5 bg-danger rounded-full flex items-center justify-center">
                  <span className="text-xs font-bold text-white">{unreadCount}</span>
                </div>
              )}
            </div>
            <div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                Уведомления
              </h1>
              <p className="text-foreground-600">
                {unreadCount > 0 ? `${unreadCount} непрочитанных уведомлений` : 'Все уведомления прочитаны'}
              </p>
            </div>
          </div>
        </div>

        <div className="w-full md:w-96">
          <Input
            placeholder="Поиск уведомлений..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            variant="bordered"
            glassmorphism
            leftIcon="solar:magnifer-zoom-in-bold"
            className="w-full"
          />
        </div>

        <div className="space-y-4">
          {filteredNotifications.map((notification, index) => {
            const typeConfig = getTypeConfig(notification.type);
            
            return (
              <Card
                key={notification.id}
                variant="glass"
                className={`
                  backdrop-blur-xl bg-background/30 border shadow-xl transition-all duration-300 hover:shadow-2xl
                  ${notification.isRead ? 'border-divider/20' : 'border-primary/30 bg-primary/5'}
                  animate-in fade-in-0 slide-in-from-left-4
                `}
                style={{ animationDelay: `${index * 100}ms` }}
              >
                <div className="p-6">
                  <div className="flex items-start gap-4">
                    <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl flex-shrink-0 border bg-primary/10 border-primary/20">
                      <Icon icon={typeConfig.icon} className={`w-6 h-6 text-${typeConfig.color}`} />
                    </div>

                    <div className="flex-1 min-w-0 space-y-2">
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <h3 className={`font-semibold ${notification.isRead ? 'text-foreground' : 'text-foreground font-bold'}`}>
                              {notification.title}
                            </h3>
                            {!notification.isRead && (
                              <div className="w-2 h-2 bg-primary rounded-full"></div>
                            )}
                          </div>
                          
                          <p className="text-foreground-600 text-sm leading-relaxed">
                            {notification.message}
                          </p>
                        </div>

                        <div className="flex items-center gap-2">
                          {!notification.isRead && (
                            <Button
                              variant="ghost"
                              size="sm"
                              icon="solar:check-circle-bold"
                              onClick={() => handleMarkAsRead(notification.id)}
                              className="text-success hover:bg-success/10"
                            />
                          )}
                        </div>
                      </div>

                      <div className="flex items-center justify-between pt-2">
                        <Chip
                          color={typeConfig.color}
                          size="sm"
                        >
                          {notification.type}
                        </Chip>
                        
                        <span className="text-xs text-foreground-500">
                          {formatTimeAgo(notification.timestamp)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </Card>
            );
          })}
        </div>

        {filteredNotifications.length === 0 && (
          <div className="text-center py-12">
            <Icon icon="solar:bell-off-bold" className="w-16 h-16 text-foreground-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-foreground mb-2">
              Уведомления не найдены
            </h3>
            <p className="text-foreground-600">
              Попробуйте изменить критерии поиска
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Notifications; 