import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { Button, Card, Input, Alert } from "../components/ui";
import { Avatar, Badge } from "@heroui/react";
import { Icon } from '@iconify/react';

const Profile: React.FC = () => {
  const { user } = useAuth();
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [formData, setFormData] = useState({
    username: user?.username || '',
    email: user?.email || '',
    firstName: '',
    lastName: '',
    phone: '',
    bio: '',
    avatar: null as File | null,
  });

  useEffect(() => {
    if (user) {
      setFormData(prev => ({
        ...prev,
        username: user.username || '',
        email: user.email || '',
      }));
    }
  }, [user]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // TODO: Implement API call to update profile
      await new Promise(resolve => setTimeout(resolve, 1000)); // Mock API call
      setSuccess(true);
      setEditing(false);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err: any) {
      setError(err.message || 'Ошибка сохранения профиля');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setEditing(false);
    setError(null);
    if (user) {
      setFormData(prev => ({
        ...prev,
        username: user.username || '',
        email: user.email || '',
      }));
    }
  };

  const profileStats = [
    { label: 'Операций', value: '247', icon: 'solar:graph-up-bold' },
    { label: 'Активов', value: '12', icon: 'solar:wallet-money-bold' },
    { label: 'Прибыль', value: '+15.8%', icon: 'solar:chart-2-bold' },
    { label: 'Рейтинг', value: '4.8', icon: 'solar:star-bold' },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6">
      <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        
        {/* Header */}
        <div className="text-center space-y-2">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
            Профиль пользователя
          </h1>
          <p className="text-foreground-600 text-lg">
            Управляйте своей учетной записью и настройками
          </p>
        </div>

        {/* Profile Header Card */}
        <Card
          variant="glass"
          className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-top-4 duration-700"
          style={{ animationDelay: '200ms' }}
        >
          <div className="p-8">
            <div className="flex flex-col md:flex-row items-center gap-6">
              
              {/* Avatar Section */}
              <div className="relative">
                <Badge 
                  color="success"
                  content=""
                  size="md"
                  placement="bottom-right"
                >
                  <Avatar 
                    size="lg"
                    name={user?.username || 'User'}
                    className="w-24 h-24 text-2xl"
                    color="primary"
                  />
                </Badge>
              </div>

              {/* User Info */}
              <div className="flex-1 text-center md:text-left space-y-3">
                <div className="space-y-1">
                  <h2 className="text-3xl font-bold text-foreground">
                    {user?.username || 'Пользователь'}
                  </h2>
                  <p className="text-foreground-600 text-lg">
                    {user?.email || 'email@example.com'}
                  </p>
                  <div className="flex items-center justify-center md:justify-start gap-2 text-sm text-foreground-500">
                    <Icon icon="solar:calendar-bold" className="w-4 h-4" />
                    <span>Присоединился в январе 2024</span>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="flex gap-3 justify-center md:justify-start">
                  {!editing ? (
                    <Button
                      variant="primary"
                      size="lg"
                      gradient
                      icon="solar:pen-bold"
                      onClick={() => setEditing(true)}
                    >
                      Редактировать профиль
                    </Button>
                  ) : (
                    <div className="flex gap-2">
                      <Button
                        variant="primary"
                        size="md"
                        gradient
                        icon="solar:check-circle-bold"
                        onClick={handleSave}
                        disabled={loading}
                      >
                        {loading ? 'Сохранение...' : 'Сохранить'}
                      </Button>
                      <Button
                        variant="ghost"
                        size="md"
                        icon="solar:close-circle-bold"
                        onClick={handleCancel}
                        disabled={loading}
                      >
                        Отмена
                      </Button>
                    </div>
                  )}
                </div>
              </div>

              {/* Stats Grid */}
              <div className="grid grid-cols-2 gap-4 min-w-fit">
                {profileStats.map((stat, index) => (
                  <div key={stat.label} className="text-center space-y-1">
                    <div className="flex items-center justify-center w-10 h-10 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-lg backdrop-blur-sm">
                      <Icon icon={stat.icon} className="w-5 h-5 text-primary" />
                    </div>
                    <div className="text-xl font-bold text-foreground">{stat.value}</div>
                    <div className="text-xs text-foreground-500">{stat.label}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </Card>

        {/* Profile Details */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* Personal Information */}
          <Card
            variant="glass"
            className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-left-4 duration-700"
            style={{ animationDelay: '400ms' }}
          >
            <div className="p-6 space-y-6">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-lg">
                  <Icon icon="solar:user-bold" className="w-5 h-5 text-primary" />
                </div>
                <h3 className="text-xl font-semibold text-foreground">
                  Личная информация
                </h3>
              </div>

              <form onSubmit={handleSave} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <Input
                    label="Имя"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    variant="bordered"
                    disabled={!editing}
                    glassmorphism
                    leftIcon="solar:user-bold"
                    placeholder="Введите имя"
                  />
                  <Input
                    label="Фамилия"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    variant="bordered"
                    disabled={!editing}
                    glassmorphism
                    leftIcon="solar:user-bold"
                    placeholder="Введите фамилию"
                  />
                </div>
                
                <Input
                  label="Имя пользователя"
                  name="username"
                  value={formData.username}
                  onChange={handleInputChange}
                  variant="bordered"
                  disabled={!editing}
                  glassmorphism
                  leftIcon="solar:user-id-bold"
                  placeholder="Введите имя пользователя"
                />
                
                <Input
                  label="Email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  variant="bordered"
                  disabled={!editing}
                  glassmorphism
                  leftIcon="solar:letter-bold"
                  placeholder="Введите email"
                />
                
                <Input
                  label="Телефон"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  variant="bordered"
                  disabled={!editing}
                  glassmorphism
                  leftIcon="solar:phone-bold"
                  placeholder="Введите номер телефона"
                />
              </form>
            </div>
          </Card>

          {/* Additional Information */}
          <Card
            variant="glass"
            className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-right-4 duration-700"
            style={{ animationDelay: '500ms' }}
          >
            <div className="p-6 space-y-6">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-gradient-to-r from-secondary/20 to-primary/20 rounded-lg">
                  <Icon icon="solar:settings-bold" className="w-5 h-5 text-secondary" />
                </div>
                <h3 className="text-xl font-semibold text-foreground">
                  Дополнительно
                </h3>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-foreground-600 mb-2">
                    О себе
                  </label>
                  <textarea
                    name="bio"
                    value={formData.bio}
                    onChange={handleInputChange}
                    disabled={!editing}
                    rows={4}
                    className="w-full px-4 py-3 bg-background/50 border border-divider/30 rounded-lg backdrop-blur-sm text-foreground placeholder-foreground-500 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary/50 transition-colors"
                    placeholder="Расскажите о себе..."
                  />
                </div>

                {/* Activity Section */}
                <div className="space-y-3">
                  <h4 className="text-sm font-medium text-foreground-600">Последняя активность</h4>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between py-2 px-3 bg-background/30 rounded-lg">
                      <div className="flex items-center gap-2">
                        <Icon icon="solar:login-3-bold" className="w-4 h-4 text-success" />
                        <span className="text-sm text-foreground-600">Последний вход</span>
                      </div>
                      <span className="text-sm text-foreground">Сегодня, 14:30</span>
                    </div>
                    <div className="flex items-center justify-between py-2 px-3 bg-background/30 rounded-lg">
                      <div className="flex items-center gap-2">
                        <Icon icon="solar:shield-check-bold" className="w-4 h-4 text-primary" />
                        <span className="text-sm text-foreground-600">Верификация</span>
                      </div>
                      <span className="text-sm text-success">Подтверждено</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </Card>
        </div>

        {/* Alerts */}
        {success && (
          <div className="animate-in fade-in-0 slide-in-from-top-2 duration-300">
            <Alert
              type="success"
              title="Профиль обновлен"
              description="Ваши данные успешно сохранены"
              variant="glass"
            />
          </div>
        )}
        
        {error && (
          <div className="animate-in fade-in-0 slide-in-from-top-2 duration-300">
            <Alert
              type="error"
              title="Ошибка сохранения"
              description={error}
              variant="glass"
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default Profile; 