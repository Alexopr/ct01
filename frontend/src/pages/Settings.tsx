import React, { useState } from "react";
import { Button, Card, Input, Alert } from "../components/ui";
import { Tabs, Tab, Switch, Select, SelectItem } from "@heroui/react";
import { Icon } from '@iconify/react';

const Settings: React.FC = () => {
  const [activeTab, setActiveTab] = useState("account");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Account Settings
  const [accountData, setAccountData] = useState({
    email: "user@example.com",
    password: "",
    confirmPassword: "",
    currentPassword: "",
  });

  // App Settings
  const [appSettings, setAppSettings] = useState({
    notifications: true,
    emailNotifications: false,
    darkMode: true,
    language: "ru",
    currency: "USD",
    autoRefresh: true,
    refreshInterval: "30",
  });

  // Security Settings
  const [securitySettings, setSecuritySettings] = useState({
    twoFactorAuth: false,
    loginAlerts: true,
    sessionTimeout: "60",
    deviceManagement: true,
  });

  const handleAccountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setAccountData(prev => ({ ...prev, [name]: value }));
  };

  const handleSaveAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // TODO: Implement API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err: any) {
      setError(err.message || 'Ошибка сохранения настроек');
    } finally {
      setLoading(false);
    }
  };

  const handleAppSettingChange = (key: string, value: any) => {
    setAppSettings(prev => ({ ...prev, [key]: value }));
  };

  const handleSecuritySettingChange = (key: string, value: any) => {
    setSecuritySettings(prev => ({ ...prev, [key]: value }));
  };

  const languages = [
    { key: "ru", label: "Русский" },
    { key: "en", label: "English" },
    { key: "de", label: "Deutsch" },
  ];

  const currencies = [
    { key: "USD", label: "USD - Доллар США" },
    { key: "EUR", label: "EUR - Евро" },
    { key: "RUB", label: "RUB - Российский рубль" },
    { key: "BTC", label: "BTC - Bitcoin" },
  ];

  const refreshIntervals = [
    { key: "10", label: "10 секунд" },
    { key: "30", label: "30 секунд" },
    { key: "60", label: "1 минута" },
    { key: "300", label: "5 минут" },
  ];

  const sessionTimeouts = [
    { key: "30", label: "30 минут" },
    { key: "60", label: "1 час" },
    { key: "240", label: "4 часа" },
    { key: "480", label: "8 часов" },
    { key: "never", label: "Никогда" },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6">
      <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        
        {/* Header */}
        <div className="text-center space-y-2">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
            Настройки
          </h1>
          <p className="text-foreground-600 text-lg">
            Настройте приложение под свои предпочтения
          </p>
        </div>

        {/* Main Settings Card */}
        <Card
          variant="glass"
          className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-top-4 duration-700"
          style={{ animationDelay: '200ms' }}
        >
          <div className="p-6">
            <Tabs
              selectedKey={activeTab}
              onSelectionChange={(key) => setActiveTab(key as string)}
              variant="underlined"
              classNames={{
                tabList: "gap-6 w-full relative rounded-none p-0 border-b border-divider/20",
                cursor: "w-full bg-gradient-to-r from-primary to-secondary",
                tab: "max-w-fit px-0 h-12",
                tabContent: "group-data-[selected=true]:text-primary"
              }}
            >
              <Tab
                key="account"
                title={
                  <div className="flex items-center space-x-2">
                    <Icon icon="solar:user-bold" className="w-4 h-4" />
                    <span>Аккаунт</span>
                  </div>
                }
              >
                {/* Account Settings */}
                <div className="mt-6 space-y-6">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="p-2 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-lg">
                      <Icon icon="solar:user-bold" className="w-5 h-5 text-primary" />
                    </div>
                    <h3 className="text-xl font-semibold text-foreground">
                      Настройки аккаунта
                    </h3>
                  </div>

                  <form onSubmit={handleSaveAccount} className="space-y-4">
                    <Input
                      label="Email"
                      name="email"
                      type="email"
                      value={accountData.email}
                      onChange={handleAccountChange}
                      variant="bordered"
                      glassmorphism
                      leftIcon="solar:letter-bold"
                      placeholder="Введите email"
                    />
                    
                    <Input
                      label="Текущий пароль"
                      name="currentPassword"
                      type="password"
                      value={accountData.currentPassword}
                      onChange={handleAccountChange}
                      variant="bordered"
                      glassmorphism
                      leftIcon="solar:lock-password-bold"
                      placeholder="Введите текущий пароль"
                    />
                    
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <Input
                        label="Новый пароль"
                        name="password"
                        type="password"
                        value={accountData.password}
                        onChange={handleAccountChange}
                        variant="bordered"
                        glassmorphism
                        leftIcon="solar:lock-password-unlocked-bold"
                        placeholder="Новый пароль"
                      />
                      <Input
                        label="Подтвердите пароль"
                        name="confirmPassword"
                        type="password"
                        value={accountData.confirmPassword}
                        onChange={handleAccountChange}
                        variant="bordered"
                        glassmorphism
                        leftIcon="solar:lock-password-unlocked-bold"
                        placeholder="Подтвердите пароль"
                      />
                    </div>

                    <div className="flex justify-end pt-4">
                      <Button
                        type="submit"
                        variant="primary"
                        size="lg"
                        gradient
                        disabled={loading}
                        icon="solar:check-circle-bold"
                      >
                        {loading ? 'Сохранение...' : 'Сохранить изменения'}
                      </Button>
                    </div>
                  </form>
                </div>
              </Tab>

              <Tab
                key="app"
                title={
                  <div className="flex items-center space-x-2">
                    <Icon icon="solar:settings-bold" className="w-4 h-4" />
                    <span>Приложение</span>
                  </div>
                }
              >
                {/* App Settings */}
                <div className="mt-6 space-y-6">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="p-2 bg-gradient-to-r from-secondary/20 to-primary/20 rounded-lg">
                      <Icon icon="solar:settings-bold" className="w-5 h-5 text-secondary" />
                    </div>
                    <h3 className="text-xl font-semibold text-foreground">
                      Настройки приложения
                    </h3>
                  </div>

                  <div className="space-y-6">
                    {/* Notifications */}
                    <div className="flex items-center justify-between">
                      <div className="space-y-1">
                        <h4 className="text-medium font-semibold text-foreground">Уведомления</h4>
                        <p className="text-sm text-foreground-500">Включить push-уведомления</p>
                      </div>
                      <Switch
                        isSelected={appSettings.notifications}
                        onValueChange={(value) => handleAppSettingChange('notifications', value)}
                        color="primary"
                      />
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="space-y-1">
                        <h4 className="text-medium font-semibold text-foreground">Email уведомления</h4>
                        <p className="text-sm text-foreground-500">Получать уведомления на email</p>
                      </div>
                      <Switch
                        isSelected={appSettings.emailNotifications}
                        onValueChange={(value) => handleAppSettingChange('emailNotifications', value)}
                        color="primary"
                      />
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="space-y-1">
                        <h4 className="text-medium font-semibold text-foreground">Автообновление</h4>
                        <p className="text-sm text-foreground-500">Автоматически обновлять данные</p>
                      </div>
                      <Switch
                        isSelected={appSettings.autoRefresh}
                        onValueChange={(value) => handleAppSettingChange('autoRefresh', value)}
                        color="primary"
                      />
                    </div>

                    {/* Dropdowns */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <Select
                        label="Язык интерфейса"
                        selectedKeys={[appSettings.language]}
                        onSelectionChange={(keys) => handleAppSettingChange('language', Array.from(keys)[0])}
                        variant="bordered"
                        startContent={<Icon icon="solar:translation-bold" className="w-4 h-4" />}
                      >
                        {languages.map((lang) => (
                          <SelectItem key={lang.key} value={lang.key}>
                            {lang.label}
                          </SelectItem>
                        ))}
                      </Select>

                      <Select
                        label="Валюта по умолчанию"
                        selectedKeys={[appSettings.currency]}
                        onSelectionChange={(keys) => handleAppSettingChange('currency', Array.from(keys)[0])}
                        variant="bordered"
                        startContent={<Icon icon="solar:wallet-money-bold" className="w-4 h-4" />}
                      >
                        {currencies.map((currency) => (
                          <SelectItem key={currency.key} value={currency.key}>
                            {currency.label}
                          </SelectItem>
                        ))}
                      </Select>
                    </div>

                    {appSettings.autoRefresh && (
                      <Select
                        label="Интервал обновления"
                        selectedKeys={[appSettings.refreshInterval]}
                        onSelectionChange={(keys) => handleAppSettingChange('refreshInterval', Array.from(keys)[0])}
                        variant="bordered"
                        startContent={<Icon icon="solar:refresh-bold" className="w-4 h-4" />}
                      >
                        {refreshIntervals.map((interval) => (
                          <SelectItem key={interval.key} value={interval.key}>
                            {interval.label}
                          </SelectItem>
                        ))}
                      </Select>
                    )}
                  </div>
                </div>
              </Tab>

              <Tab
                key="security"
                title={
                  <div className="flex items-center space-x-2">
                    <Icon icon="solar:shield-bold" className="w-4 h-4" />
                    <span>Безопасность</span>
                  </div>
                }
              >
                {/* Security Settings */}
                <div className="mt-6 space-y-6">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="p-2 bg-gradient-to-r from-warning/20 to-danger/20 rounded-lg">
                      <Icon icon="solar:shield-bold" className="w-5 h-5 text-warning" />
                    </div>
                    <h3 className="text-xl font-semibold text-foreground">
                      Настройки безопасности
                    </h3>
                  </div>

                  <div className="space-y-6">
                    <div className="flex items-center justify-between">
                      <div className="space-y-1">
                        <h4 className="text-medium font-semibold text-foreground">Двухфакторная аутентификация</h4>
                        <p className="text-sm text-foreground-500">Дополнительный уровень защиты</p>
                      </div>
                      <Switch
                        isSelected={securitySettings.twoFactorAuth}
                        onValueChange={(value) => handleSecuritySettingChange('twoFactorAuth', value)}
                        color="warning"
                      />
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="space-y-1">
                        <h4 className="text-medium font-semibold text-foreground">Уведомления о входе</h4>
                        <p className="text-sm text-foreground-500">Уведомлять о новых входах в аккаунт</p>
                      </div>
                      <Switch
                        isSelected={securitySettings.loginAlerts}
                        onValueChange={(value) => handleSecuritySettingChange('loginAlerts', value)}
                        color="warning"
                      />
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="space-y-1">
                        <h4 className="text-medium font-semibold text-foreground">Управление устройствами</h4>
                        <p className="text-sm text-foreground-500">Отслеживать активные сессии</p>
                      </div>
                      <Switch
                        isSelected={securitySettings.deviceManagement}
                        onValueChange={(value) => handleSecuritySettingChange('deviceManagement', value)}
                        color="warning"
                      />
                    </div>

                    <Select
                      label="Тайм-аут сессии"
                      selectedKeys={[securitySettings.sessionTimeout]}
                      onSelectionChange={(keys) => handleSecuritySettingChange('sessionTimeout', Array.from(keys)[0])}
                      variant="bordered"
                      startContent={<Icon icon="solar:clock-circle-bold" className="w-4 h-4" />}
                    >
                      {sessionTimeouts.map((timeout) => (
                        <SelectItem key={timeout.key} value={timeout.key}>
                          {timeout.label}
                        </SelectItem>
                      ))}
                    </Select>
                  </div>
                </div>
              </Tab>
            </Tabs>
          </div>
        </Card>

        {/* Alerts */}
        {success && (
          <div className="animate-in fade-in-0 slide-in-from-top-2 duration-300">
            <Alert
              type="success"
              title="Настройки сохранены"
              description="Ваши настройки успешно обновлены"
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

export default Settings; 