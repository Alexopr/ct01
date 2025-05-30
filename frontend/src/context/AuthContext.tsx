import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../utils/api';
import type { User, TelegramUser } from '../types/auth';
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
      const response = await api.get('/auth/me');
      
      // Маппим ответ сервера в наш тип User
      const userData = response.data;
      if (userData && userData.username) {
        const mappedUser: User = {
          id: userData.id || 0,
          telegramId: userData.telegramId || 0,
          username: userData.username,
          firstName: userData.firstName || '',
          lastName: userData.lastName,
          photoUrl: userData.photoUrl,
          roles: userData.roles || ['USER'],
          createdAt: userData.createdAt || new Date().toISOString(),
          updatedAt: userData.updatedAt || new Date().toISOString(),
          email: userData.email,
        };
        setUser(mappedUser);
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
      await api.post('/auth/login', { username, password });
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
      
      const response = await api.post('/auth/telegram', telegramData);
      const authResponse = response.data;
      
      if (authResponse.user) {
        // Маппим ответ в наш тип User
        const mappedUser: User = {
          id: authResponse.user.id || 0,
          telegramId: telegramData.id,
          username: authResponse.user.username || telegramData.username,
          firstName: telegramData.first_name,
          lastName: telegramData.last_name,
          photoUrl: telegramData.photo_url,
          roles: authResponse.user.roles || ['USER'],
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          email: authResponse.user.email,
        };
        
        setUser(mappedUser);
        setLoading(false);
        toast.success(`Добро пожаловать, ${mappedUser.firstName || mappedUser.username}!`);
        return mappedUser;
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
      await api.post('/auth/logout');
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
  const isAdmin = () => user?.roles?.includes('ADMIN') || false;

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