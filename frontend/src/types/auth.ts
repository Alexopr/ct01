export interface TelegramUser {
  id: number;
  first_name: string;
  last_name?: string;
  username?: string;
  photo_url?: string;
  auth_date: number;
  hash: string;
}

export interface User {
  id: number;
  telegramId: number;
  username?: string;
  email?: string;
  firstName: string;
  lastName?: string;
  photoUrl?: string;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  user: User;
  message: string;
}

export interface TelegramAuthModalProps {
  open: boolean;
  onClose: () => void;
  onAuthSuccess?: (user: User) => void;
} 