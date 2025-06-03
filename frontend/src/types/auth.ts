import type { User } from './api';

export interface TelegramUser {
  id: number;
  first_name: string;
  last_name?: string;
  username?: string;
  photo_url?: string;
  auth_date: number;
  hash: string;
}

export interface AuthResponse {
  message: string;
  user: User;
}

export interface TelegramAuthModalProps {
  open: boolean;
  onClose: () => void;
  onAuthSuccess?: (user: User) => void;
} 
