import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../utils/api';
import type { User } from '../types/api';
import type { TelegramUser } from '../types/auth';
import toast from 'react-hot-toast';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: string | null;
  login: (username: string, password: string) => Promise<void>;
  loginWithTelegram: (telegramData: TelegramUser) => Promise<User>;
  logout: () => void;
  isAuthenticated: () => boolean;
  isAdmin: () => boolean;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{children: React.ReactNode}> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchUser();
  }, []);

  const fetchUser = async () => {
    try {
      const response = await api.get('/v1/auth/me');
      
      // Используем данные напрямую, так как backend уже возвращает правильную структуру
      const userData = response.data;
      if (userData && userData.username) {
        setUser(userData);
      } else {
        setUser(null);
      }
      setLoading(false);
    } catch (err) {
      setUser(null);
      setLoading(false);
    }
  };

  const login = async (username: string, password: string) => {
    try {
      setError(null);
      await api.post('/v1/auth/login', { username, password });
      await fetchUser();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed');
      throw err;
    }
  };

  const loginWithTelegram = async (telegramData: TelegramUser): Promise<User> => {
    try {
      setError(null);
      setLoading(true);
      
      const response = await api.post('/v1/auth/telegram', telegramData);
      const authResponse = response.data;
      
      if (authResponse.user) {
        setUser(authResponse.user);
        setLoading(false);
        toast.success(`Добро пожаловать, ${authResponse.user.firstName || authResponse.user.username}!`);
        return authResponse.user;
      } else {
        throw new Error('Не получен пользователь от сервера');
      }
    } catch (err: any) {
      setLoading(false);
      const errorMessage = err.response?.data?.message || 'Ошибка авторизации через Telegram';
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  };

  const logout = async () => {
    try {
      await api.post('/v1/auth/logout');
    } catch (err) {
      // Игнорируем ошибки logout
    } finally {
    setUser(null);
      localStorage.removeItem('token');
      toast.success('Вы успешно вышли из системы');
      // Перезагружаем страницу для полной очистки состояния
      setTimeout(() => window.location.reload(), 1000);
    }
  };

  const refreshUser = async () => {
    await fetchUser();
  };

  const isAuthenticated = () => !!user;
  const isAdmin = () => user?.roles?.some(role => role.name === 'ADMIN') || false;

  return (
    <AuthContext.Provider value={{ 
      user, 
      loading, 
      error, 
      login, 
      loginWithTelegram,
      logout, 
      isAuthenticated, 
      isAdmin,
      refreshUser 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}; 
