import React from "react";
import { Card, Button } from '../components/ui';
import { Icon } from '@iconify/react';
import { useNavigate } from 'react-router-dom';

const NotFound: React.FC = () => {
  const navigate = useNavigate();

  const quickLinks = [
    {
      title: 'Главная страница',
      description: 'Вернуться на главную',
      icon: 'solar:home-bold',
      path: '/',
      color: 'primary'
    },
    {
      title: 'Панель управления',
      description: 'Перейти в дашборд',
      icon: 'solar:widget-6-bold',
      path: '/dashboard',
      color: 'secondary'
    },
    {
      title: 'Инструменты',
      description: 'Торговые инструменты',
      icon: 'solar:settings-bold',
      path: '/tools',
      color: 'success'
    },
    {
      title: 'О платформе',
      description: 'Узнать больше',
      icon: 'solar:info-circle-bold',
      path: '/about',
      color: 'warning'
    },
  ];

  const getColorClasses = (color: string) => {
    switch (color) {
      case 'primary':
        return {
          iconBg: 'from-primary/20 to-primary/30',
          iconColor: 'text-primary',
          hoverBg: 'hover:bg-primary/10'
        };
      case 'secondary':
        return {
          iconBg: 'from-secondary/20 to-secondary/30',
          iconColor: 'text-secondary',
          hoverBg: 'hover:bg-secondary/10'
        };
      case 'success':
        return {
          iconBg: 'from-success/20 to-success/30',
          iconColor: 'text-success',
          hoverBg: 'hover:bg-success/10'
        };
      case 'warning':
        return {
          iconBg: 'from-warning/20 to-warning/30',
          iconColor: 'text-warning',
          hoverBg: 'hover:bg-warning/10'
        };
      default:
        return {
          iconBg: 'from-primary/20 to-primary/30',
          iconColor: 'text-primary',
          hoverBg: 'hover:bg-primary/10'
        };
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 flex items-center justify-center p-6">
      <div className="max-w-4xl mx-auto text-center space-y-12 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        
        {/* 404 Illustration */}
        <div className="space-y-8">
          {/* Animated 404 */}
          <div className="relative">
            {/* Background glow */}
            <div className="absolute inset-0 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-full blur-3xl scale-150 animate-pulse"></div>
            
            {/* 404 Text */}
            <div className="relative">
              <h1 className="text-8xl md:text-9xl font-black bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent animate-in fade-in-0 slide-in-from-top-4 duration-700">
                404
              </h1>
              
              {/* Floating elements */}
              <div className="absolute -top-4 -left-4 w-8 h-8 bg-gradient-to-r from-primary to-secondary rounded-full animate-bounce" style={{ animationDelay: '0.5s' }}></div>
              <div className="absolute -top-8 right-8 w-6 h-6 bg-gradient-to-r from-secondary to-warning rounded-full animate-bounce" style={{ animationDelay: '1s' }}></div>
              <div className="absolute -bottom-4 left-12 w-4 h-4 bg-gradient-to-r from-success to-primary rounded-full animate-bounce" style={{ animationDelay: '1.5s' }}></div>
            </div>
          </div>

          {/* Error Message */}
          <div className="space-y-4 animate-in fade-in-0 slide-in-from-bottom-4 duration-700" style={{ animationDelay: '300ms' }}>
            <h2 className="text-3xl md:text-4xl font-bold text-foreground">
              Страница не найдена
            </h2>
            <p className="text-lg text-foreground-600 max-w-2xl mx-auto leading-relaxed">
              Упс! Похоже, что страница, которую вы ищете, не существует или была перемещена. 
              Не волнуйтесь, мы поможем вам найти то, что нужно.
            </p>
          </div>
        </div>

        {/* Quick Navigation */}
        <div className="space-y-8">
          <div className="space-y-4">
            <h3 className="text-xl font-semibold text-foreground">
              Куда бы вы хотели перейти?
            </h3>
            <p className="text-foreground-600">
              Выберите один из популярных разделов нашей платформы
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-2xl mx-auto">
            {quickLinks.map((link, index) => {
              const colorClasses = getColorClasses(link.color);
              
              return (
                <Card
                  key={link.title}
                  variant="glass"
                  hoverable
                  className={`backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl transition-all duration-300 hover:shadow-2xl cursor-pointer ${colorClasses.hoverBg} animate-in fade-in-0 slide-in-from-bottom-4`}
                  style={{ animationDelay: `${(index + 2) * 150}ms` }}
                  onClick={() => navigate(link.path)}
                >
                  <div className="p-6 flex items-center gap-4">
                    <div className={`inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r ${colorClasses.iconBg} rounded-xl flex-shrink-0`}>
                      <Icon icon={link.icon} className={`w-6 h-6 ${colorClasses.iconColor}`} />
                    </div>
                    
                    <div className="text-left flex-1">
                      <h4 className="font-semibold text-foreground mb-1">
                        {link.title}
                      </h4>
                      <p className="text-sm text-foreground-600">
                        {link.description}
                      </p>
                    </div>
                    
                    <Icon icon="solar:arrow-right-bold" className="w-5 h-5 text-foreground-400" />
                  </div>
                </Card>
              );
            })}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center animate-in fade-in-0 slide-in-from-bottom-4 duration-700" style={{ animationDelay: '900ms' }}>
          <Button
            variant="primary"
            size="lg"
            gradient
            icon="solar:home-bold"
            onClick={() => navigate('/')}
            className="px-8"
          >
            На главную
          </Button>
          <Button
            variant="ghost"
            size="lg"
            icon="solar:arrow-left-bold"
            onClick={() => window.history.back()}
            className="px-8 border border-divider/30 hover:border-primary/50"
          >
            Назад
          </Button>
        </div>

        {/* Help Section */}
        <Card
          variant="glass"
          className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-bottom-4 duration-700"
          style={{ animationDelay: '1200ms' }}
        >
          <div className="p-6 text-center space-y-4">
            <div className="inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r from-info/20 to-info/30 rounded-xl">
              <Icon icon="solar:question-circle-bold" className="w-6 h-6 text-info" />
            </div>
            
            <div>
              <h4 className="font-semibold text-foreground mb-2">
                Нужна помощь?
              </h4>
              <p className="text-sm text-foreground-600 mb-4">
                Если вы считаете, что это ошибка, или у вас есть вопросы, 
                свяжитесь с нашей службой поддержки.
              </p>
              
              <Button
                variant="ghost"
                size="sm"
                icon="solar:chat-round-dots-bold"
                className="border border-divider/30 hover:border-info/50"
              >
                Связаться с поддержкой
              </Button>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default NotFound; 