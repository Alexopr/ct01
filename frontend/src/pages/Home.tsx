import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Button, Card, Modal, ModalContent, ModalHeader, ModalBody, ModalFooter } from '../components/ui';
import { Icon } from '@iconify/react';

const advantages = [
  { 
    icon: 'solar:shield-check-bold',
    title: 'Безопасно',
    description: 'Надежная защита ваших активов'
  },
  { 
    icon: 'solar:bolt-bold',
    title: 'Быстро',
    description: 'Мгновенные операции и анализ'
  },
  { 
    icon: 'solar:chart-2-bold',
    title: 'Аналитика',
    description: 'Глубокая аналитика рынка'
  },
  { 
    icon: 'solar:wallet-money-bold',
    title: 'Мультивалютность',
    description: 'Поддержка всех криптовалют'
  },
];

const stats = [
  { value: '50K+', label: 'Активных пользователей' },
  { value: '200+', label: 'Поддерживаемых монет' },
  { value: '24/7', label: 'Техническая поддержка' },
  { value: '99.9%', label: 'Время безотказной работы' },
];

const Home: React.FC = () => {
  const [modalOpen, setModalOpen] = useState(false);
  const navigate = useNavigate();
  const { user } = useAuth();

  const handleGetStarted = () => setModalOpen(true);
  const handleCloseModal = () => setModalOpen(false);
  const handleLogin = () => { setModalOpen(false); navigate('/login'); };
  const handleRegister = () => { setModalOpen(false); navigate('/register'); };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 overflow-hidden">
      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center px-4 py-12">
        {/* Background Animation Elements */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-1/4 left-1/4 w-64 h-64 bg-gradient-to-r from-primary/10 to-secondary/10 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-gradient-to-r from-secondary/10 to-primary/10 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }}></div>
        </div>

        <div className="relative z-10 w-full max-w-7xl mx-auto">
          <div className="text-center space-y-8 animate-in fade-in-0 slide-in-from-bottom-8 duration-1000">
            {/* Main Hero Content */}
            <div className="space-y-6">
              <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-r from-primary to-secondary rounded-full p-1 animate-in fade-in-0 slide-in-from-top-4 duration-700">
                <div className="w-full h-full bg-background rounded-full flex items-center justify-center">
                  <Icon icon="solar:wallet-money-bold" className="w-10 h-10 text-primary" />
                </div>
              </div>
              
              <h1 className="text-6xl md:text-8xl font-bold bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
                Crypto Dashboard
              </h1>
              
              <p className="text-xl md:text-2xl text-foreground-600 max-w-3xl mx-auto leading-relaxed animate-in fade-in-0 slide-in-from-bottom-4 duration-1000" style={{ animationDelay: '200ms' }}>
                Современная платформа для управления крипто-активами и аналитики.
                <br />
                <span className="text-primary font-semibold">Всё, что нужно — в одном месте.</span>
              </p>
            </div>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center animate-in fade-in-0 slide-in-from-bottom-4 duration-1000" style={{ animationDelay: '400ms' }}>
              {user ? (
                <>
                  <div className="text-center space-y-4">
                    <p className="text-lg text-success font-semibold">
                      Добро пожаловать, {user.username || 'пользователь'}!
                    </p>
                    <div className="flex gap-4">
                      <Button
                        variant="primary"
                        size="lg"
                        gradient
                        icon="solar:chart-2-bold"
                        onClick={() => navigate('/dashboard')}
                        className="px-8 py-4 text-lg"
                      >
                        Перейти к аналитике
                      </Button>
                      <Button
                        variant="secondary"
                        size="lg"
                        icon="solar:user-circle-bold"
                        onClick={() => navigate('/profile')}
                        className="px-8 py-4 text-lg"
                      >
                        Профиль
                      </Button>
                    </div>
                  </div>
                </>
              ) : (
                <>
                  <Button
                    variant="primary"
                    size="lg"
                    gradient
                    icon="solar:rocket-bold"
                    onClick={handleGetStarted}
                    className="px-12 py-6 text-xl font-bold shadow-2xl hover:shadow-primary/50 transition-all duration-500 hover:scale-105"
                  >
                    Начать сейчас
                  </Button>
                  <Button
                    variant="ghost"
                    size="lg"
                    icon="solar:play-circle-bold"
                    className="px-8 py-6 text-lg border border-divider/30 hover:border-primary/50"
                  >
                    Смотреть демо
                  </Button>
                </>
              )}
            </div>

            {/* Statistics */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-8 max-w-4xl mx-auto pt-12 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000" style={{ animationDelay: '600ms' }}>
              {stats.map((stat, index) => (
                <div key={stat.label} className="text-center space-y-2">
                  <div className="text-3xl md:text-4xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                    {stat.value}
                  </div>
                  <div className="text-sm text-foreground-500 font-medium">
                    {stat.label}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center space-y-4 mb-16 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
            <h2 className="text-4xl md:text-5xl font-bold text-foreground">
              Почему выбирают нас
            </h2>
            <p className="text-xl text-foreground-600 max-w-2xl mx-auto">
              Надежные инструменты для профессиональной работы с криптовалютами
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {advantages.map((advantage, index) => (
              <Card
                key={advantage.title}
                variant="glass"
                hoverable
                className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl transition-all duration-500 hover:shadow-2xl hover:shadow-primary/10 animate-in fade-in-0 slide-in-from-bottom-4"
                style={{ animationDelay: `${index * 150}ms` }}
                padding="lg"
              >
                <div className="text-center space-y-4">
                  <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-2xl backdrop-blur-sm">
                    <Icon icon={advantage.icon} className="w-8 h-8 text-primary" />
                  </div>
                  
                  <div className="space-y-2">
                    <h3 className="text-xl font-semibold text-foreground">
                      {advantage.title}
                    </h3>
                    <p className="text-sm text-foreground-600">
                      {advantage.description}
                    </p>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-4">
        <div className="max-w-4xl mx-auto text-center">
          <Card
            variant="gradient"
            className="bg-gradient-to-r from-primary to-secondary p-12 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000"
          >
            <div className="space-y-6">
              <h2 className="text-3xl md:text-4xl font-bold text-white">
                Готовы начать торговлю?
              </h2>
              <p className="text-xl text-white/90 max-w-2xl mx-auto">
                Присоединяйтесь к тысячам трейдеров, которые уже используют нашу платформу
              </p>
              
              {!user && (
                <div className="flex flex-col sm:flex-row gap-4 justify-center">
                  <Button
                    variant="ghost"
                    size="lg"
                    onClick={handleGetStarted}
                    className="bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 px-8 py-4"
                  >
                    Создать аккаунт
                  </Button>
                  <Button
                    variant="ghost"
                    size="lg"
                    onClick={() => navigate('/login')}
                    className="bg-transparent border border-white/50 text-white hover:bg-white/10 px-8 py-4"
                  >
                    Войти в аккаунт
                  </Button>
                </div>
              )}
            </div>
          </Card>
        </div>
      </section>

      {/* Modal */}
      <Modal 
        isOpen={modalOpen} 
        onClose={handleCloseModal}
        variant="glass"
        size="sm"
      >
        <ModalContent>
          <ModalHeader className="text-center">
            <h3 className="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              Выберите действие
            </h3>
          </ModalHeader>
          
          <ModalBody className="text-center space-y-4">
            <p className="text-foreground-600">
              Войдите в аккаунт или зарегистрируйтесь для доступа к платформе.
            </p>
          </ModalBody>
          
          <ModalFooter className="flex gap-4 justify-center">
            <Button
              variant="primary"
              size="lg"
              gradient
              onClick={handleLogin}
              icon="solar:login-3-bold"
              className="flex-1"
            >
              Войти
            </Button>
            <Button
              variant="secondary"
              size="lg"
              onClick={handleRegister}
              icon="solar:user-plus-bold"
              className="flex-1"
            >
              Зарегистрироваться
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </div>
  );
};

export default Home;