import React from "react";
import { Card, Button } from '../components/ui';
import { Icon } from '@iconify/react';
import { useNavigate } from 'react-router-dom';

const features = [
  {
    title: 'Единая панель управления',
    description: 'Управляйте всеми крипто-активами из одного места',
    icon: 'solar:widget-6-bold',
    color: 'primary'
  },
  {
    title: 'Мгновенная аналитика',
    description: 'Получайте актуальные данные и графики в реальном времени',
    icon: 'solar:chart-2-bold',
    color: 'secondary'
  },
  {
    title: 'Безопасность данных',
    description: 'Высокий уровень защиты и приватности ваших данных',
    icon: 'solar:shield-check-bold',
    color: 'success'
  },
  {
    title: 'Адаптивный дизайн',
    description: 'Удобный интерфейс для любых устройств и экранов',
    icon: 'solar:devices-bold',
    color: 'warning'
  },
  {
    title: 'Современные технологии',
    description: 'Быстрый и отзывчивый интерфейс на базе React и TypeScript',
    icon: 'solar:code-bold',
    color: 'danger'
  },
];

const stats = [
  { label: 'Пользователей', value: '10K+', icon: 'solar:users-group-rounded-bold' },
  { label: 'Транзакций', value: '1M+', icon: 'solar:transfer-horizontal-bold' },
  { label: 'Бирж', value: '50+', icon: 'solar:buildings-3-bold' },
  { label: 'Криптовалют', value: '500+', icon: 'solar:bitcoin-bold' },
];

const About: React.FC = () => {
  const navigate = useNavigate();

  const getColorClasses = (color: string) => {
    switch (color) {
      case 'primary':
        return {
          iconBg: 'from-primary/20 to-primary/30',
          iconColor: 'text-primary'
        };
      case 'secondary':
        return {
          iconBg: 'from-secondary/20 to-secondary/30',
          iconColor: 'text-secondary'
        };
      case 'success':
        return {
          iconBg: 'from-success/20 to-success/30',
          iconColor: 'text-success'
        };
      case 'warning':
        return {
          iconBg: 'from-warning/20 to-warning/30',
          iconColor: 'text-warning'
        };
      case 'danger':
        return {
          iconBg: 'from-danger/20 to-danger/30',
          iconColor: 'text-danger'
        };
      default:
        return {
          iconBg: 'from-primary/20 to-primary/30',
          iconColor: 'text-primary'
        };
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6">
      <div className="max-w-6xl mx-auto space-y-12 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        
        {/* Hero Section */}
        <div className="text-center space-y-6">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-primary to-secondary rounded-3xl p-1 animate-in fade-in-0 slide-in-from-top-4 duration-700">
            <div className="w-full h-full bg-background rounded-2xl flex items-center justify-center">
              <Icon icon="solar:chart-square-bold" className="w-10 h-10 text-primary" />
            </div>
          </div>
          
          <div className="space-y-4">
            <h1 className="text-5xl md:text-6xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              О платформе
            </h1>
            <p className="text-xl text-foreground-600 max-w-3xl mx-auto leading-relaxed">
              Crypto Dashboard — это современная платформа для управления криптовалютными активами, 
              аналитики и мониторинга рынка в реальном времени.
            </p>
          </div>
        </div>

        {/* Stats Section */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {stats.map((stat, index) => (
            <Card
              key={stat.label}
              variant="glass"
              hoverable
              className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl transition-all duration-500 hover:shadow-2xl animate-in fade-in-0 slide-in-from-bottom-4"
              style={{ animationDelay: `${index * 100}ms` }}
            >
              <div className="p-6 text-center space-y-3">
                <div className="inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r from-primary/20 to-primary/30 rounded-xl">
                  <Icon icon={stat.icon} className="w-6 h-6 text-primary" />
                </div>
                <div>
                  <div className="text-2xl font-bold text-foreground">{stat.value}</div>
                  <div className="text-sm text-foreground-600">{stat.label}</div>
                </div>
              </div>
            </Card>
          ))}
        </div>

        {/* Features Section */}
        <div className="space-y-8">
          <div className="text-center">
            <h2 className="text-3xl font-bold text-foreground mb-4">
              Возможности платформы
            </h2>
            <p className="text-foreground-600 max-w-2xl mx-auto">
              Мы создали комплексное решение для работы с криптовалютами, 
              объединив лучшие практики и современные технологии.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {features.map((feature, index) => {
              const colorClasses = getColorClasses(feature.color);
              
              return (
                <Card
                  key={feature.title}
                  variant="glass"
                  hoverable
                  className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl transition-all duration-500 hover:shadow-2xl animate-in fade-in-0 slide-in-from-bottom-4"
                  style={{ animationDelay: `${(index + 4) * 150}ms` }}
                >
                  <div className="p-6 space-y-4">
                    <div className={`inline-flex items-center justify-center w-14 h-14 bg-gradient-to-r ${colorClasses.iconBg} rounded-2xl`}>
                      <Icon icon={feature.icon} className={`w-7 h-7 ${colorClasses.iconColor}`} />
                    </div>
                    
                    <div className="space-y-2">
                      <h3 className="text-lg font-semibold text-foreground">
                        {feature.title}
                      </h3>
                      <p className="text-sm text-foreground-600 leading-relaxed">
                        {feature.description}
                      </p>
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>
        </div>

        {/* Technology Section */}
        <Card
          variant="glass"
          className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-bottom-4 duration-700"
          style={{ animationDelay: '1200ms' }}
        >
          <div className="p-8 text-center space-y-6">
            <div className="space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-primary to-secondary rounded-2xl">
                <Icon icon="solar:code-2-bold" className="w-8 h-8 text-white" />
              </div>
              
              <div>
                <h2 className="text-2xl font-bold text-foreground mb-2">
                  Технологический стек
                </h2>
                <p className="text-foreground-600 max-w-3xl mx-auto">
                  Платформа построена на современных технологиях, обеспечивающих высокую производительность, 
                  безопасность и удобство использования.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 max-w-2xl mx-auto">
              {[
                { name: 'React', icon: 'logos:react' },
                { name: 'TypeScript', icon: 'logos:typescript-icon' },
                { name: 'Spring Boot', icon: 'logos:spring-icon' },
                { name: 'PostgreSQL', icon: 'logos:postgresql' },
              ].map((tech, index) => (
                <div
                  key={tech.name}
                  className="flex flex-col items-center gap-2 p-4 bg-background/50 rounded-lg border border-divider/20 hover:bg-background/70 transition-colors duration-200"
                >
                  <Icon icon={tech.icon} className="w-8 h-8" />
                  <span className="text-sm font-medium text-foreground">{tech.name}</span>
                </div>
              ))}
            </div>
          </div>
        </Card>

        {/* CTA Section */}
        <div className="text-center space-y-6">
          <div className="space-y-4">
            <h2 className="text-3xl font-bold text-foreground">
              Готовы начать?
            </h2>
            <p className="text-foreground-600 max-w-2xl mx-auto">
              Присоединяйтесь к тысячам пользователей, которые уже используют нашу платформу 
              для управления своими криптовалютными активами.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button
              variant="primary"
              size="lg"
              gradient
              icon="solar:rocket-bold"
              onClick={() => navigate('/register')}
              className="px-8"
            >
              Начать использование
            </Button>
            <Button
              variant="ghost"
              size="lg"
              icon="solar:home-bold"
              onClick={() => navigate('/')}
              className="px-8 border border-divider/30 hover:border-primary/50"
            >
              На главную
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default About;