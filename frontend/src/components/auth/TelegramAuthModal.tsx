import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  CircularProgress,
  Alert,
  Divider,
  IconButton,
} from '@mui/material';
import { Close } from '@mui/icons-material';
import { styled } from '@mui/material/styles';
import type { TelegramAuthModalProps, TelegramUser } from '../../types/auth';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

// Стилизованные компоненты с glassmorphism эффектом
const StyledDialog = styled(Dialog)(({ theme }) => ({
  '& .MuiDialog-paper': {
    background: 'rgba(35,38,58,0.95)',
    backdropFilter: 'blur(20px)',
    border: '1px solid rgba(108,71,255,0.1)',
    borderRadius: theme.spacing(3),
    boxShadow: '0 4px 32px 0 rgba(108,71,255,0.25), 0 1.5px 8px 0 rgba(0,228,255,0.10)',
    minWidth: '400px',
    maxWidth: '500px',
    overflow: 'visible',
  },
}));

const StyledDialogTitle = styled(DialogTitle)(({ theme }) => ({
  background: 'linear-gradient(135deg, #6C47FF 0%, #00E4FF 100%)',
  WebkitBackgroundClip: 'text',
  WebkitTextFillColor: 'transparent',
  backgroundClip: 'text',
  fontWeight: 700,
  fontSize: '1.5rem',
  textAlign: 'center',
  padding: theme.spacing(3, 3, 2),
  position: 'relative',
}));

const TelegramWidgetContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  minHeight: '80px',
  padding: theme.spacing(2),
  borderRadius: theme.spacing(2),
  background: 'rgba(255,255,255,0.03)',
  border: '1px solid rgba(255,255,255,0.08)',
  margin: theme.spacing(2, 0),
}));

const LoadingContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: theme.spacing(2),
  padding: theme.spacing(3),
}));

declare global {
  interface Window {
    onTelegramAuth: (user: TelegramUser) => void;
  }
}

const TelegramAuthModal: React.FC<TelegramAuthModalProps> = ({
  open,
  onClose,
  onAuthSuccess,
}) => {
  const { loginWithTelegram } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [widgetLoaded, setWidgetLoaded] = useState(false);

  // Очистка состояния при открытии/закрытии модалки
  useEffect(() => {
    if (open) {
      setError(null);
      setLoading(false);
      loadTelegramWidget();
    } else {
      // Очистка при закрытии
      setWidgetLoaded(false);
      if (window.onTelegramAuth) {
        window.onTelegramAuth = undefined as any;
      }
    }
  }, [open]);

  const loadTelegramWidget = async () => {
    try {
      setLoading(true);
      
      // Проверяем, загружен ли уже скрипт
      if (!document.getElementById('telegram-widget-script')) {
        const script = document.createElement('script');
        script.id = 'telegram-widget-script';
        script.src = 'https://telegram.org/js/telegram-widget.js?22';
        script.setAttribute('data-telegram-login', 'alg_gor_bot');
        script.setAttribute('data-size', 'large');
        script.setAttribute('data-radius', '20');
        script.setAttribute('data-onauth', 'onTelegramAuth(user)');
        script.setAttribute('data-request-access', 'write');
        
        // Устанавливаем callback функцию
        window.onTelegramAuth = handleTelegramAuth;
        
        script.onload = () => {
          setWidgetLoaded(true);
          setLoading(false);
        };
        
        script.onerror = () => {
          const errorMsg = 'Не удалось загрузить виджет Telegram';
          setError(errorMsg);
          setLoading(false);
          toast.error(errorMsg);
        };
        
        // Добавляем в контейнер виджета
        const container = document.getElementById('telegram-widget-container');
        if (container) {
          container.appendChild(script);
        }
      } else {
        // Скрипт уже загружен
        window.onTelegramAuth = handleTelegramAuth;
        setWidgetLoaded(true);
        setLoading(false);
      }
    } catch (err) {
      const errorMsg = 'Ошибка при загрузке виджета авторизации';
      setError(errorMsg);
      setLoading(false);
      toast.error(errorMsg);
    }
  };

  const handleTelegramAuth = async (telegramUser: TelegramUser) => {
    try {
      setLoading(true);
      setError(null);
      
      // Используем метод из AuthContext
      const user = await loginWithTelegram(telegramUser);
      
      // Вызываем callback при успешной авторизации
      if (onAuthSuccess) {
        onAuthSuccess(user);
      }
      
      // Закрываем модалку
      onClose();
      
    } catch (err: any) {
      setError(err.message || 'Произошла ошибка при авторизации');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      onClose();
    }
  };

  return (
    <StyledDialog
      open={open}
      onClose={handleClose}
      aria-labelledby="telegram-auth-title"
      maxWidth="sm"
      fullWidth
    >
      <StyledDialogTitle id="telegram-auth-title">
        <IconButton
          aria-label="close"
          onClick={handleClose}
          disabled={loading}
          sx={{
            position: 'absolute',
            right: 16,
            top: 16,
            color: 'rgba(255,255,255,0.7)',
            '&:hover': {
              color: '#fff',
              background: 'rgba(255,255,255,0.1)',
            },
          }}
        >
          <Close />
        </IconButton>
        Вход через Telegram
      </StyledDialogTitle>

      <DialogContent sx={{ px: 3, pb: 2 }}>
        <Typography variant="body2" color="text.secondary" textAlign="center" mb={2}>
          Для доступа к платформе используйте свой аккаунт Telegram
        </Typography>

        <Divider sx={{ my: 2, borderColor: 'rgba(255,255,255,0.08)' }} />

        {error && (
          <Alert 
            severity="error" 
            sx={{ 
              mb: 2,
              background: 'rgba(244,67,54,0.1)',
              border: '1px solid rgba(244,67,54,0.2)',
              '& .MuiAlert-icon': {
                color: '#f44336',
              },
            }}
          >
            {error}
          </Alert>
        )}

        <TelegramWidgetContainer id="telegram-widget-container">
          {loading && (
            <LoadingContainer>
              <CircularProgress size={24} sx={{ color: '#6C47FF' }} />
              <Typography variant="body2" color="text.secondary">
                Загрузка виджета авторизации...
              </Typography>
            </LoadingContainer>
          )}
          
          {!loading && !widgetLoaded && (
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Виджет Telegram недоступен
              </Typography>
              <Button
                variant="contained"
                onClick={() => {
                  // Тестовые данные пользователя
                  const testUser = {
                    id: 123456789,
                    first_name: "Test",
                    last_name: "User",
                    username: "testuser",
                    photo_url: "",
                    auth_date: Math.floor(Date.now() / 1000),
                    hash: "test_hash"
                  };
                  handleTelegramAuth(testUser);
                }}
                sx={{
                  background: 'linear-gradient(135deg, #0088cc 0%, #0066aa 100%)',
                  color: 'white',
                  '&:hover': {
                    background: 'linear-gradient(135deg, #0066aa 0%, #004488 100%)',
                  },
                }}
              >
                Войти как тестовый пользователь
              </Button>
            </Box>
          )}
        </TelegramWidgetContainer>

        <Typography variant="caption" color="text.secondary" textAlign="center" display="block" mt={2}>
          Нажимая "Войти через Telegram", вы соглашаетесь с условиями использования
        </Typography>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 3 }}>
        <Button
          onClick={handleClose}
          disabled={loading}
          variant="outlined"
          sx={{
            borderColor: 'rgba(255,255,255,0.2)',
            color: 'text.secondary',
            '&:hover': {
              borderColor: 'rgba(255,255,255,0.4)',
              background: 'rgba(255,255,255,0.05)',
            },
          }}
        >
          Отмена
        </Button>
      </DialogActions>
    </StyledDialog>
  );
};

export default TelegramAuthModal; 