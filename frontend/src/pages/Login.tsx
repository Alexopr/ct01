import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { TelegramAuthModal } from "../components/auth";
import { Button, Input, Card, Alert } from "../components/ui";
import { Link } from "@heroui/react";

const Login: React.FC = () => {
  const { login, error } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [tgOpen, setTgOpen] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(username, password);
      navigate('/');
    } catch {}
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/90 to-background/80 flex items-center justify-center p-4">
      <div className="w-full max-w-md animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        <Card
          variant="glass"
          className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-2xl"
          padding="lg"
        >
          <div className="text-center space-y-6">
            {/* Header */}
            <div className="space-y-2">
              <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                Вход в аккаунт
              </h1>
              <p className="text-foreground-600 text-sm">
                Войдите в свой аккаунт для продолжения работы
              </p>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                label="Имя пользователя"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                variant="bordered"
                fullWidth
                required
                autoFocus
                leftIcon="solar:user-bold"
                glassmorphism
                placeholder="Введите имя пользователя"
              />
              
              <Input
                label="Пароль"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                variant="bordered"
                fullWidth
                required
                leftIcon="solar:lock-password-bold"
                glassmorphism
                placeholder="Введите пароль"
              />

              <Button
                type="submit"
                variant="primary"
                size="lg"
                fullWidth
                gradient
                disabled={loading}
                className="transition-all duration-300 hover:shadow-xl hover:shadow-primary/25"
              >
                {loading ? "Вход..." : "Войти"}
              </Button>

              <Button
                variant="ghost"
                size="lg"
                fullWidth
                onClick={() => setTgOpen(true)}
                icon="logos:telegram"
                className="border border-divider/30 hover:border-primary/50 transition-all duration-300"
              >
                Войти через Telegram
              </Button>

              <div className="pt-2">
                <Link
                  href="/forgot-password"
                  className="text-sm text-foreground-600 hover:text-primary transition-colors duration-200 underline-offset-4 hover:underline"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/forgot-password');
                  }}
                >
                  Забыли пароль?
                </Link>
              </div>

              {error && (
                <div className="animate-in fade-in-0 slide-in-from-top-2 duration-300">
                  <Alert
                    type="error"
                    title="Ошибка входа"
                    description={error}
                    variant="glass"
                  />
                </div>
              )}
            </form>

            {/* Footer */}
            <div className="pt-4 border-t border-divider/20">
              <p className="text-sm text-foreground-600">
                Нет аккаунта?{" "}
                <Link
                  href="/register"
                  className="text-primary hover:text-primary-400 transition-colors duration-200 font-medium"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/register');
                  }}
                >
                  Зарегистрироваться
                </Link>
              </p>
            </div>
          </div>
        </Card>

        <TelegramAuthModal open={tgOpen} onClose={() => setTgOpen(false)} />
      </div>
    </div>
  );
};

export default Login; 